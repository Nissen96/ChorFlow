package chorflow.ast

import chorflow.visitor.Visitor

class ProcedureCall(
    val id: String,
    val processArguments: List<String>,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}
