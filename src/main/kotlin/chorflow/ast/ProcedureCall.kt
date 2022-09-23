package chorflow.ast

import chorflow.util.forEach
import chorflow.visitor.Visitor

class ProcedureCall(
    val id: String,
    val processArguments: List<Process>,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        processArguments.forEach({ it.accept(visitor) }, doBetween = { visitor.midVisit(this) })
        visitor.postVisit(this)
    }
}
