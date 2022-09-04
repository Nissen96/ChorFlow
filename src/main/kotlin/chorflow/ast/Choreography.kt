package chorflow.ast

import chorflow.visitor.Visitor

class Choreography(
    val instruction: Instruction,
    val continuation: Choreography?,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        instruction.accept(visitor)
        visitor.postVisit(this)
        continuation?.accept(visitor)
    }
}