package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.flow.Flow
import chorflow.flow.FlowMapper
import chorflow.flow.TypeChecker
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
    val flows = mutableSetOf<Pair<String, String>>()

    File(filename).forEachLine { line ->
        if (line.isNotBlank() && !line.startsWith("//")) {
            val (src, dests) = line.replace(" ", "").split("->")
            if (dests.isNotBlank()) {
                dests.split(",").forEach { dest ->
                    flows.add(Pair(src, dest))
                }
            }
        }
    }

    return Flow(flows)
}

fun main(args: Array<String>) {
    val program = parseChoreographyFile(args[0])

    // Pretty print
    program.accept(PrettyPrintVisitor(indentation = 4, condensed = false))
    println()
    program.accept(PrettyPrintVisitor(indentation = 4, condensed = true))
    println()

    // Read and print flow policy
    val policy = parseFlowFile(args[1])
    println("POLICY:")
    policy.flows.forEach { println(it.first + " -> " + it.second) }
    println()

    // Type check the choreography based on the provided flow mapping function and flow policy
    val flowMapper = FlowMapper()  // Hardcoded for now
    val typeChecker = TypeChecker(program.procedures, flowMapper, policy)

    program.choreography.accept(typeChecker)
    if (typeChecker.errors.isNotEmpty()) {
        throw Exception("Flow violations found:\n" + typeChecker.errors.joinToString("\n"))
    }

    println("CHORFLOW:")
    typeChecker.flow.flows.forEach { println(it.first + " -> " + it.second) }
}
