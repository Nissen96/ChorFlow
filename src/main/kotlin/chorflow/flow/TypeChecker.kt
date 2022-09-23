package chorflow.flow

import chorflow.ast.*
import chorflow.visitor.SubstitutionVisitor
import chorflow.visitor.Visitor

class TypeChecker(
    private val procedures: List<Procedure>,
    private val flowMapper: FlowMapper,
    private val policy: Flow
): Visitor() {
    val errors = mutableListOf<String>()
    var flow = Flow()

    // Store each argument list passed to each procedure to avoid re-checking on recursion
    private val procedureCalls = procedures.associate { it.id to mutableSetOf<List<String>>() }

    private fun checkPolicy(astNode: ASTNode) {
        // Map event to flows
        val flows = when (astNode) {
            is Assignment -> flowMapper.flows(astNode)
            is Conditional -> flowMapper.flows(astNode)
            is Selection -> flowMapper.flows(astNode)
            is Interaction -> flowMapper.flows(astNode)
            else -> throw IllegalArgumentException("flow mapping function is not defined for this event")
        }

        // Add flow to total program flow
        flow += flows

        // Add any flow violations
        flows.flows.forEach {
            if (it !in policy) {
                errors.add("${it.first} -> ${it.second} (line ${astNode.lineNumber}, index ${astNode.charPosition})")
            }
        }
    }

    override fun visit(assignment: Assignment) {
        checkPolicy(assignment)
    }

    override fun midVisit(selection: Selection) {
        checkPolicy(selection)
    }

    override fun preVisit(conditional: Conditional) {
        checkPolicy(conditional)
    }

    override fun preMidVisit(interaction: Interaction) {
        checkPolicy(interaction)
    }

    override fun preVisit(procedureCall: ProcedureCall) {
        // Find matching procedure
        val procedure = procedures.find { it.id == procedureCall.id }!!
        if (procedureCall.processArguments.size != procedure.processParameters.size) {
            throw UnsupportedOperationException("Number of arguments do not match expected number of parameters")
        }

        val parameters = procedure.processParameters.map { it.id }
        val arguments = procedureCall.processArguments.map { it.id }

        // Substitute all process parameters with passed process arguments
        procedure.accept(SubstitutionVisitor(parameters, arguments))

        // Type check resulting procedure recursively - if not done previously for the same argument list!
        if (arguments !in procedureCalls[procedure.id]!!) {
            procedureCalls[procedure.id]!!.add(arguments)
            procedure.accept(this)
        }
    }
}