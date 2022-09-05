package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.grammar.ChorLexer
import chorflow.grammar.ChorParser
import chorflow.visitor.PrettyPrintVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val input = CharStreams.fromFileName(args[0])
    val lexer = ChorLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = ChorParser(tokens)
    val cst = parser.program()
    val ast = ASTVisitor().visit(cst) as Program
    ast.accept(PrettyPrintVisitor(indentation = 4, condensed = "-c" in args))
}
