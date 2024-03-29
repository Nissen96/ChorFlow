package chorflow.ast

import chorflow.visitor.Visitor
import chorflow.util.forEach

class Program(
    val procedures: List<Procedure>,
    val choreography: Choreography?,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        procedures.forEach({ it.accept(visitor) }, doBetween = { visitor.preMidVisit(this) })
        visitor.postMidVisit(this)
        choreography?.accept(visitor)
        visitor.postVisit(this)
    }
}
