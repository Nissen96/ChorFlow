package chorflow.ast

import chorflow.visitor.Visitor

class Expression(
    val expression: String,
    val variables: Set<String>,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}
