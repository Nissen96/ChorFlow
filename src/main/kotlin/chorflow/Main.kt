package chorflow

import chorflow.grammar.ChorLexer
import chorflow.grammar.ChorParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val input = CharStreams.fromStream(System.`in`)
    val lexer = ChorLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = ChorParser(tokens)
    println("Main run")
}