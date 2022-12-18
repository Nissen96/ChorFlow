package chorflow.flow

import chorflow.ast.*
import chorflow.visitor.Visitor

class TypeChecker(
    private val flowMapper: FlowMapper,
    private val localPolicies: Map<String, Flow>
): Visitor() {
    val policyErrors = mutableListOf<String>()
    val localPolicyErrors = localPolicies.keys.associateWith { mutableListOf<String>() }
    private lateinit var errors: MutableList<String>
    private lateinit var procedures: List<Procedure>

    private fun checkPolicy(event: Event, policy: Flow): Flow {
        // Map event to its flows and checkFlow satisfaction
        val flow = flowMapper.flow(event)
        if (!flow.isSubflow(policy)) {
            // Mark each violation
            event as ASTNode
            flow.flows.forEach {
                if (it !in policy) {
                    errors.add("${it.first} -> ${it.second} (line ${event.lineNumber}, index ${event.charPosition})")
                }
            }
        }
        return flow
    }

    fun checkFlow(program: Program, policy: Flow): Pair<Flow, Map<String, Flow>> {
        procedures = program.procedures
        val localFlows = procedures.associate { it.id to checkFlow(it) }
        errors = policyErrors
        return checkFlow(program.choreography, policy) to localFlows
    }

    private fun checkFlow(procedure: Procedure): Flow {
        // Check local policy satisfaction for procedure body
        val localPolicy = localPolicies[procedure.id] ?: Flow()
        errors = localPolicyErrors[procedure.id] ?: mutableListOf()
        return checkFlow(procedure.choreography, localPolicy)
    }

    private fun checkFlow(choreography: Choreography?, policy: Flow): Flow {
        if (choreography == null) {
            return Flow()
        }
        var flow = when (choreography.instruction) {
            is Action -> checkPolicy(choreography.instruction, policy)
            is Conditional -> checkFlow(choreography.instruction, policy)
            is ProcedureCall -> checkFlow(choreography.instruction, policy)
            else -> throw IllegalArgumentException("No typing rules for this instruction")
        }
        if (choreography.continuation != null) {
            flow += checkFlow(choreography.continuation, policy)
        }
        return flow
    }

    private fun checkFlow(conditional: Conditional, policy: Flow): Flow {
        return checkPolicy(conditional.guard, policy) +
                checkFlow(conditional.ifChoreography, policy) +
                checkFlow(conditional.elseChoreography, policy)
    }

    private fun checkFlow(procedureCall: ProcedureCall, policy: Flow): Flow {
        // Find matching procedure and validate arguments
        val procedure = procedures.find { it.id == procedureCall.id }!!
        if (procedureCall.processArguments.size != procedure.processParameters.size) {
            throw UnsupportedOperationException("Number of arguments do not match number of parameters")
        }

        // Generate substitution mapping
        val parameters = procedure.processParameters.map { it.id }
        val arguments = procedureCall.processArguments.map { it.id }
        val substitutions = parameters.zip(arguments).toMap()

        // Substitution in local policy
        val localPolicy = localPolicies[procedure.id] ?: Flow()
        val instantiatedLocalPolicy = Flow(localPolicy.flows.map {flow ->
            // Extract and substitute process id
            val src = flow.first.split(".")
            val dest = flow.second.split(".")
            var mappedSrc = substitutions[src[0]]
                ?: throw NoSuchElementException("Substitution failed for process ${src[0]}. Please check choreography and policy")
            var mappedDest = substitutions[dest[0]]
                ?: throw NoSuchElementException("Substitution failed for process ${dest[0]}. Please check choreography and policy")

            // Add back potential variable
            if (src.size > 1) mappedSrc += ".${src[1]}"
            if (dest.size > 1) mappedDest += ".${dest[1]}"

            mappedSrc to mappedDest
        }.toMutableSet())

        // check compatability with global policy
        if (!instantiatedLocalPolicy.isSubflow(policy)) {
            instantiatedLocalPolicy.flows.forEach {
                if (it !in policy) {
                    errors.add("${it.first} -> ${it.second} (in procedure ${procedureCall.id})")
                }
            }
        }

        return instantiatedLocalPolicy
    }
}
