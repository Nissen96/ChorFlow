package chorflow.ast

import chorflow.visitor.Visitor

class Conditional(
    val process: Process,
    val expression: Expression,
    val ifChoreography: Choreography,
    val elseChoreography: Choreography,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        process.accept(visitor)
        visitor.preMidVisit(this)
        expression.accept(visitor)
        visitor.midMidVisit(this)
        ifChoreography.accept(visitor)
        visitor.postMidVisit(this)
        elseChoreography.accept(visitor)
        visitor.postVisit(this)
    }
}
