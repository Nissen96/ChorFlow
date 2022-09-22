package chorflow.ast

import chorflow.flow.Flow
import chorflow.util.forEach
import chorflow.visitor.Visitor

class Program(
    val procedures: List<Procedure>,
    val choreography: Choreography,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    var flow: Flow = Flow()
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        // TODO procedures should be visited separately, allowing process substitution
        //procedures.forEach({ it.accept(visitor) }, doBetween = { visitor.preMidVisit(this) })
        visitor.postMidVisit(this)
        choreography.accept(visitor)
        visitor.postVisit(this)
    }
}
