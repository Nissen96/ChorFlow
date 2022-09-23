package chorflow.ast

import chorflow.util.forEach
import chorflow.visitor.Visitor

class Procedure(
    val id: String,
    val processParameters: List<Process>,
    val choreography: Choreography,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        processParameters.forEach({ it.accept(visitor) }, doBetween = { visitor.preMidVisit(this) })
        visitor.postMidVisit(this)
        choreography.accept(visitor)
        visitor.postVisit(this)
    }
}
