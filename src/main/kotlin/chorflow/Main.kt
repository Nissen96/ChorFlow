package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.flow.*
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

fun parsePolicyFile(filename: String): Pair<Flow, Map<String, Flow>> {
    val policy = Flow()
    val localPolicies = mutableMapOf<String, Flow>()

    var currentPolicy = policy
    File(filename).forEachLine { line ->
        // Skip blank lines and comments
        if (line.isNotBlank() && !line.startsWith("#")) {
            // Switch policy on new label, else add flow to policy
            if (line.endsWith(":")) {
                currentPolicy = Flow()
                localPolicies[line.dropLast(1)] = currentPolicy
            } else {
                val (src, dests) = line.filter { !it.isWhitespace() }.split("->")
                if (dests.isNotBlank()) {
                    dests.split(",").forEach { dest ->
                        currentPolicy.add(src to dest)
                    }
                }
            }
        }
    }
    return policy to localPolicies
}

fun parseMappingFile(filename: String): MappingSpec {
    val mappingSpec = MappingSpec()

    val eventPattern = Regex("""flow\((p\.x := e | p\.e | p -> q\[L] | p\.e -> q\.x)\) = (.*)""", RegexOption.COMMENTS)
    val flowPattern = Regex("""([pqex]) (<-|<->|->) ([pqex])""", RegexOption.COMMENTS)

    File(filename).forEachLine { line ->
        if (line.isNotBlank() && !line.startsWith("#")) {
            val eventMatch = eventPattern.matchEntire(line.filter { !it.isWhitespace() })!!
            val eventType = eventMatch.groupValues[1]
            val mappingList = with (eventType) {
                when {
                    contains("p.x:=e") -> mappingSpec.assignment
                    contains("p->q[L]") -> mappingSpec.selection
                    contains("p.e->q.x") -> mappingSpec.interaction
                    contains("p.e") -> mappingSpec.conditional
                    else -> throw IllegalArgumentException("Invalid flow mapping function")
                }
            }

            flowPattern.findAll(eventMatch.groupValues[2]).forEach {
                val src = it.groupValues[1]
                val dir = it.groupValues[2]
                val dst = it.groupValues[3]
                when (dir) {
                    "->" -> mappingList.add(src to dst)
                    "<-" -> mappingList.add(dst to src)
                    "<->" -> {
                        mappingList.add(src to dst)
                        mappingList.add(dst to src)
                    }
                    else -> throw UnsupportedOperationException("Invalid flow direction")
                }
            }
        }
    }

    return mappingSpec
}

fun main(args: Array<String>) {
    val program = parseChoreographyFile(args[0])

    // Pretty print
    program.accept(PrettyPrintVisitor(indentation = 4, condensed = false))
    println()

    // Parse user-defined policies and flow mapping functions
    val (policy, localPolicies) = parsePolicyFile(args[1])
    val mappingSpec = parseMappingFile(args[2])
    val flowMapper = FlowMapper(mappingSpec)

    // Check policy satisfaction through type checking
    val typeChecker = TypeChecker(flowMapper, localPolicies)
    val (flow, localFlows) = typeChecker.checkFlow(program, policy)

    // Visualize flow and save it
    var flowGraph = FlowGraph(flow, policy)
    flowGraph.display()
    flowGraph.save("flow.png")

    // Visualize local flows
    localFlows.forEach { (name, flow) ->
        flowGraph = FlowGraph(flow, localPolicies[name]!!)
        flowGraph.display()
        flowGraph.save("${name}.png")
    }

    // Build error message from local and global flow violations found
    var errorMsg = ""

    typeChecker.localPolicyErrors.forEach { (name, errors) ->
        if (errors.isNotEmpty()) {
            errorMsg += "\nLocal flow violations found for procedure $name:\n\t${errors.joinToString("\n\t")}"
        }
    }

    if (typeChecker.policyErrors.isNotEmpty()) {
        errorMsg += "\nFlow violations found:\n\t${typeChecker.policyErrors.joinToString("\n\t")}"
    }

    // Throw exception if any flow violation was found
    if (errorMsg.isNotBlank()) {
        throw Exception(errorMsg)
    }
}
