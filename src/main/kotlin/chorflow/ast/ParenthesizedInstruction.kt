package chorflow.ast

import chorflow.visitor.Visitor

class ParenthesizedInstruction(
    val instruction: Instruction,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        instruction.accept(visitor)
        visitor.postVisit(this)
    }
}