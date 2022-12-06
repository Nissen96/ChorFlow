package chorflow.ast

import chorflow.visitor.Visitor

class Guard(
    val process: Process,
    val expression: Expression,
    lineNumber: Int,
    charPosition: Int
): Instruction(lineNumber, charPosition), Event {
    override fun accept(visitor: Visitor) {
        process.accept(visitor)
        visitor.visit(this)
        expression.accept(visitor)
    }
}
