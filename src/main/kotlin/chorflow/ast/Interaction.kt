package chorflow.ast

import chorflow.visitor.Visitor

class Interaction(
    val sourceProcess: String,
    val expression: Expression,
    val destinationProcess: String,
    val destinationVariable: String,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expression.accept(visitor)
        visitor.postVisit(this)
    }
}
