package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.flow.Flow
import chorflow.grammar.ChorLexer
import chorflow.grammar.ChorParser
import chorflow.visitor.PrettyPrintVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun parseChoreographyFile(filename: String): Program {
    val chorInput = CharStreams.fromFileName(filename)
    val chorLexer = ChorLexer(chorInput)
    val chorTokens = CommonTokenStream(chorLexer)
    val chorParser = ChorParser(chorTokens)
    return ASTVisitor().visit(chorParser.program()) as Program
}

fun parseFlowFile(filename: String): Flow {
    val entities = mutableSetOf<String>()
    val flows = mutableSetOf<Pair<String, String>>()

    File(filename).forEachLine { line ->
        if (line.isNotBlank() && !line.startsWith("//")) {
            val (src, dests) = line.replace(" ", "").split("->")
            entities.add(src)

            if (dests.isNotBlank()) {
                dests.split(",").forEach { dest ->
                    entities.add(dest)
                    flows.add(Pair(src, dest))
                }
            }
        }
    }

    return Flow(entities, flows)
}

fun main(args: Array<String>) {
    val choreography = parseChoreographyFile(args[0])

    // Pretty print
    choreography.accept(PrettyPrintVisitor(indentation = 4, condensed = false))
    println()
    choreography.accept(PrettyPrintVisitor(indentation = 4, condensed = true))
    println()

    // Read and print flow policy
    val policy = parseFlowFile(args[1])
    println("POLICY:")
    policy.flows.forEach { println(it.first + " -> " + it.second) }
}
