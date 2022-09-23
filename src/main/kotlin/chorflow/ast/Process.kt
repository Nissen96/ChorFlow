package chorflow.ast;

import chorflow.visitor.Visitor

class Process(
    var id: String,
    lineNumber: Int,
    charPosition: Int
): ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}
