package chorflow.ast

import chorflow.util.forEach
import chorflow.visitor.Visitor

class Program(
    val procedures: List<Procedure>,
    val choreography: Choreography?,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        procedures.forEach({ it.accept(visitor) }, doBetween = { visitor.preMidVisit(this) })
        visitor.postMidVisit(this)
        choreography?.accept(visitor)
    }
}