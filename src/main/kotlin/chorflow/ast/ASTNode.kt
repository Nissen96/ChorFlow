package chorflow.ast

import chorflow.visitor.Visitor

abstract class ASTNode(val lineNumber: Int, val charPosition: Int) {
    abstract fun accept(visitor: Visitor)
}