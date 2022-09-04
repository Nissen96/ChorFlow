package chorflow.visitor

import chorflow.ast.*

class PrettyPrintVisitor(private val indentation: Int = 4) : Visitor() {
    private var level = 0

    private fun printIndented(text: Any = "") {
        print(" ".repeat(indentation * level) + "$text")
    }

    override fun preMidVisit(program: Program) {
        println(",")
    }

    override fun postMidVisit(program: Program) {
        println()
    }

    override fun visit(procedure: Procedure) {
        print("${procedure.id}(${procedure.processParameters.joinToString(", ")}) = ")
    }

    override fun preVisit(choreography: Choreography) {
        printIndented("")
    }

    override fun postVisit(choreography: Choreography) {
        print(";\n")
        choreography.continuation ?: printIndented("0\n")
    }

    override fun preVisit(parenthesizedInstruction: ParenthesizedInstruction) {
        print("(")
    }

    override fun postVisit(parenthesizedInstruction: ParenthesizedInstruction) {
        print(")")
    }

    override fun visit(assignment: Assignment) {
        print("${assignment.process}.${assignment.variable} := ")
    }

    override fun preVisit(conditional: Conditional) {
        print("if ${conditional.process}.")
    }

    override fun preMidVisit(conditional: Conditional) {
        print(" then\n")
        level++
    }

    override fun postMidVisit(conditional: Conditional) {
        level--
        printIndented("else\n")
        level++
    }

    override fun postVisit(conditional: Conditional) {
        level--
    }

    override fun preVisit(interaction: Interaction) {
        print("${interaction.sourceProcess}.")
    }
    override fun postVisit(interaction: Interaction) {
        print(" -> ${interaction.destinationProcess}.${interaction.destinationVariable}")
    }


    override fun visit(procedureCall: ProcedureCall) {
        print("${procedureCall.id}(${procedureCall.processArguments.joinToString(", ")})")
    }

    override fun visit(selection: Selection) {
        print("${selection.sourceProcess} -> ${selection.destinationProcess}.${selection.label}")
    }

    override fun visit(expression: Expression) {
        print(expression.expression)
    }
}
