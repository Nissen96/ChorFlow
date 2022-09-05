package chorflow.ast

import chorflow.visitor.Visitor

class Conditional(
    val process: String,
    val expression: Expression,
    val ifChoreography: Choreography,
    val elseChoreography: Choreography,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expression.accept(visitor)
        visitor.preMidVisit(this)
        ifChoreography.accept(visitor)
        visitor.postMidVisit(this)
        elseChoreography.accept(visitor)
        visitor.postVisit(this)
    }
}
