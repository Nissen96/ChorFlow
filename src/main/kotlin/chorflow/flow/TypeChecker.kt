package chorflow.flow

import chorflow.ast.*
import chorflow.visitor.Visitor

class TypeChecker(val flowMapper: FlowMapper, val policy: Flow): Visitor() {
    var errors = mutableListOf<String>()
    lateinit var root: Program

    fun checkPolicy(astNode: ASTNode) {
        // Map event to flows
        val flow = when (astNode) {
            is Assignment -> flowMapper.flows(astNode)
            is Conditional -> flowMapper.flows(astNode)
            is Selection -> flowMapper.flows(astNode)
            is Interaction -> flowMapper.flows(astNode)
            else -> throw IllegalArgumentException("flow mapping function is not defined for this event")
        }

        // Add flow to total program flow
        root.flow += flow

        // Add any flow violations
        flow.flows.forEach {
            if (it !in policy) {
                errors.add("${it.first} -> ${it.second} (line ${astNode.lineNumber}, index ${astNode.charPosition})")
            }
        }
    }

    override fun preVisit(program: Program) {
        root = program
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

    override fun postVisit(program: Program) {
        if (errors.isNotEmpty()) {
            throw Exception("Flow violations found:\n" + errors.joinToString("\n"))
        }
    }
}