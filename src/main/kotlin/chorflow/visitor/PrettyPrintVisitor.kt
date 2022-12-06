package chorflow.visitor

import chorflow.ast.*

class PrettyPrintVisitor(private val indentation: Int = 4, private val condensed: Boolean = false) : Visitor() {
    private var level = 0
    private val separator = if (condensed) " " else "\n"

    private fun printIndented(text: Any = "") {
        if (!condensed)
            print(" ".repeat(indentation * level))
        print(text)
    }

    override fun preVisit(program: Program) {
        println("/* Procedure Definitions */")
    }

    override fun preMidVisit(program: Program) {
        println(", ")
    }

    override fun postMidVisit(program: Program) {
        if (condensed) println()
        println("\n/* Choreography */")
    }

    override fun postVisit(program: Program) {
        if (condensed) println()
    }

    override fun preVisit(procedure: Procedure) {
        print("${procedure.id}(")
    }

    override fun preMidVisit(procedure: Procedure) {
        print(", ")
    }

    override fun postMidVisit(procedure: Procedure) {
        print(") =$separator")
        level++
    }

    override fun postVisit(procedure: Procedure) {
        level--
    }

    override fun preVisit(choreography: Choreography) {
        printIndented()
    }

    override fun postVisit(choreography: Choreography) {
        print(";")
        if (choreography.continuation == null) {
            print(" 0${if (condensed) "" else "\n"}")
        } else {
            print(separator)
        }
    }

    override fun preVisit(parenthesizedInstruction: ParenthesizedInstruction) {
        print("(")
    }

    override fun postVisit(parenthesizedInstruction: ParenthesizedInstruction) {
        print(")")
    }

    override fun visit(assignment: Assignment) {
        print(".${assignment.variable} := ")
    }

    override fun preVisit(conditional: Conditional) {
        print("if ")
    }

    override fun preMidVisit(conditional: Conditional) {
        print(" then$separator")
        level++
    }

    override fun postMidVisit(conditional: Conditional) {
        level--
        if (condensed) {
            print(" else ")
        } else {
            printIndented("else\n")
        }
        level++
    }

    override fun postVisit(conditional: Conditional) {
        level--
        printIndented()
    }

    override fun visit(guard: Guard) {
        print(".")
    }

    override fun preMidVisit(interaction: Interaction) {
        print(".")
    }
    override fun postMidVisit(interaction: Interaction) {
        print(" -> ")
    }

    override fun postVisit(interaction: Interaction) {
        print(".${interaction.destinationVariable}")
    }


    override fun preVisit(procedureCall: ProcedureCall) {
        print("${procedureCall.id}(")
    }

    override fun midVisit(procedureCall: ProcedureCall) {
        print(", ")
    }

    override fun postVisit(procedureCall: ProcedureCall) {
        print(")")
    }

    override fun visit(process: Process) {
        print(process.id)
    }

    override fun midVisit(selection: Selection) {
        print(" -> ")
    }

    override fun postVisit(selection: Selection) {
        print("[${selection.label}]")
    }

    override fun visit(expression: Expression) {
        print(expression.expression)
    }
}
