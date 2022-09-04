package chorflow.ast

import chorflow.visitor.Visitor

class Assignment(
    val process: String,
    val variable: String,
    val expression: Expression,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        expression.accept(visitor)
    }
}