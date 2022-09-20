package chorflow.ast

import chorflow.visitor.Visitor

class Procedure(
    val id: String,
    val processParameters: List<String>,
    val choreography: Choreography,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        choreography.accept(visitor)
        visitor.postVisit(this)
    }
}
