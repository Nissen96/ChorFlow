package chorflow.ast

import chorflow.visitor.Visitor

class Selection(
    val sourceProcess: String,
    val destinationProcess: String,
    val label: String,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}