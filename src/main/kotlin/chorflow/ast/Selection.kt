package chorflow.ast

import chorflow.visitor.Visitor

class Selection(
    val sourceProcess: Process,
    val destinationProcess: Process,
    val label: String,
    lineNumber: Int,
    charPosition: Int
) : Instruction(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        sourceProcess.accept(visitor)
        visitor.midVisit(this)
        destinationProcess.accept(visitor)
        visitor.postVisit(this)
    }
}
