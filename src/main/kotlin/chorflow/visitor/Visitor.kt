package chorflow.visitor

import chorflow.ast.*

abstract class Visitor {
    open fun visit(assignment: Assignment) {}

    open fun preVisit(choreography: Choreography) {}
    open fun postVisit(choreography: Choreography) {}

    open fun preVisit(conditional: Conditional) {}
    open fun preMidVisit(conditional: Conditional) {}
    open fun postMidVisit(conditional: Conditional) {}
    open fun postVisit(conditional: Conditional) {}

    open fun visit(expression: Expression) {}

    open fun preVisit(interaction: Interaction) {}
    open fun postVisit(interaction: Interaction) {}

    open fun preVisit(parenthesizedInstruction: ParenthesizedInstruction) {}
    open fun postVisit(parenthesizedInstruction: ParenthesizedInstruction) {}

    open fun preVisit(procedure: Procedure) {}
    open fun postVisit(procedure: Procedure) {}

    open fun visit(procedureCall: ProcedureCall) {}

    open fun preVisit(program: Program) {}
    open fun preMidVisit(program: Program) {}
    open fun postMidVisit(program: Program) {}

    open fun visit(selection: Selection) {}
}
