package chorflow.ast

import chorflow.visitor.Visitor

class Interaction(
    val sourceProcess: Process,
    val expression: Expression,
    val destinationProcess: Process,
    val destinationVariable: String,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        sourceProcess.accept(visitor)
        visitor.preMidVisit(this)
        expression.accept(visitor)
        visitor.postMidVisit(this)
        destinationProcess.accept(visitor)
        visitor.postVisit(this)
    }
}
