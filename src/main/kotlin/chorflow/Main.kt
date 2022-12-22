package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.flow.*
import chorflow.grammar.ChorLexer
import chorflow.grammar.ChorParser
import chorflow.visitor.PrettyPrintVisitor
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun parseChoreography(choreographyFile: File): Program {
    val chorInput = CharStreams.fromFileName(choreographyFile.path)
    val chorLexer = ChorLexer(chorInput)
    val chorTokens = CommonTokenStream(chorLexer)
    val chorParser = ChorParser(chorTokens)
    return ASTVisitor().visit(chorParser.program()) as Program
}

fun parsePolicy(policyFile: File): Pair<Flow, Map<String, Flow>> {
    val policy = Flow()
    val localPolicies = mutableMapOf<String, Flow>()

    var currentPolicy = policy
    policyFile.forEachLine { line ->
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

fun parseMapping(mappingFile: File): FlowMapper {
    val flowMapper = FlowMapper()

    val eventPattern = Regex("""flow\((p\.x := e | p\.e | p -> q\[L] | p\.e -> q\.x)\) = (.*)""", RegexOption.COMMENTS)
    val flowPattern = Regex("""([pqex]) (<-|<->|->) ([pqex])""", RegexOption.COMMENTS)

    mappingFile.forEachLine { line ->
        if (line.isNotBlank() && !line.startsWith("#")) {
            val eventMatch = eventPattern.matchEntire(line.filter { !it.isWhitespace() })!!
            val eventType = eventMatch.groupValues[1]
            val mappingList = with (eventType) {
                when {
                    contains("p.x:=e") -> flowMapper.assignmentMapping
                    contains("p->q[L]") -> flowMapper.selectionMapping
                    contains("p.e->q.x") -> flowMapper.interactionMapping
                    contains("p.e") -> flowMapper.conditionalMapping
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

    return flowMapper
}

class GraphCommand : CliktCommand(help = "Display/save a flow graph for a choreography, a policy, or combined") {
    class ChorMap : OptionGroup() {
        val chor by option(
            "-c", "--chor", help = "choreography file (must be used with -m/--map)"
        ).file(mustExist = true, canBeDir = false).required()
        val flowmap by option(
            "-m", "--map", help = "flow mapping file (must be used with -c/--chor)"
        ).file(mustExist = true, canBeDir = false).required()
    }
    private val chorMap by ChorMap().cooccurring()
    private val pol by option("-p", "--pol", help = "policy file").file(mustExist = true, canBeDir = false)
    private val display by option("-d", "--display", help = "display flow graph(s)").flag()
    private val save by option("-s", "--save", help = "save flow graph(s)").flag()
    private val outputDir by option("-o", "--out", help = "output directory for graph images (default: current folder)").file(mustExist = true, canBeFile = false).default(File("."))

    override fun run() {
        if (chorMap == null && pol == null) {
            throw PrintMessage("You must specify a choreography + mapping, a policy, or all three", error = true)
        }

        if (!(display || save)) {
            throw PrintMessage("You must specify --display and/or --save to display/save the graph", error = true)
        }

        val (policy, localPolicies) = pol?.let { parsePolicy(it) } ?: Pair(Flow(), emptyMap())

        lateinit var flowGraphs: Map<String, FlowGraph>

        if (chorMap == null) {
            // Only visualize policy
            val policies = localPolicies + ("global" to policy)
            flowGraphs = policies.entries.associate {
                    (name, flow) -> "${name}-policy.png" to FlowGraph(policy = flow)
            }
        } else {
            // Parse all provided files and generate flows through type checking
            val program = parseChoreography(chorMap!!.chor)
            val flowMapper = parseMapping(chorMap!!.flowmap)
            val typeChecker = TypeChecker(flowMapper, localPolicies)
            val (flow, localFlows) = typeChecker.checkFlow(program, policy)

            flowGraphs = if (pol == null) {
                // Visualize just the choreography flow
                val choreographies = localFlows + ("main" to flow)
                choreographies.entries.associate {
                        (name, flow) -> "${name}-flow.png" to FlowGraph(flow = flow)
                }
            } else {
                // Generate combined flow + policy graph
                localFlows.entries.associate{
                        (name, localFlow) -> "${name}-flow.png" to FlowGraph(flow = localFlow, policy = localPolicies[name]!!)
                } + ("main-flow.png" to FlowGraph(flow = flow, policy = policy))
            }
        }

        if (display) {
            flowGraphs.values.forEach { it.display() }
        }

        if (save) {
            flowGraphs.forEach { (filename, graph) -> graph.save(outputDir.resolve(filename)) }
        }
    }
}

class PprintCommand : CliktCommand(help = "Parse and pretty print a choreography") {
    private val chor by argument(help = "choreography file").file(mustExist = true, canBeDir = false)
    private val indentation by option("-i", "--indent", help = "indentation width (default: 4)").int().default(4)
    private val condensed by option("-c", "--condensed", help = "condense to single line").flag()

    override fun run() {
        val program = parseChoreography(chor)
        program.accept(PrettyPrintVisitor(indentation = indentation, condensed = condensed))
    }
}

class CheckCommand : CliktCommand(help = "Check the information flow of a choreography against a flow policy") {
    private val chor by argument(help = "choreography file").file(mustExist = true, canBeDir = false)
    private val pol by argument(help = "policy file").file(mustExist = true, canBeDir = false)
    private val map by argument(help = "flow mapping file").file(mustExist = true, canBeDir = false)

    override fun run() {
        val program = parseChoreography(chor)
        val (policy, localPolicies) = parsePolicy(pol)
        val flowMapper = parseMapping(map)
        val typeChecker = TypeChecker(flowMapper, localPolicies)
        typeChecker.checkFlow(program, policy)

        var errorMsg = ""
        typeChecker.localPolicyErrors.forEach { (name, errors) ->
            if (errors.isNotEmpty()) {
                errorMsg += "Local flow violations for procedure $name:\n\t${errors.joinToString("\n\t")}\n\n"
            }
        }
        if (typeChecker.policyErrors.isNotEmpty()) {
            errorMsg += "Flow violations for main choreography:\n\t${typeChecker.policyErrors.joinToString("\n\t")}"
        }

        // Throw exception if any flow violation was found
        if (errorMsg.isNotBlank()) {
            println("\u001B[31m$errorMsg\u001B[0m")
        } else {
            println("\u001B[32mNo flow violations found, good job!\u001B[0m")
        }
    }
}

class ChorflowCommand : CliktCommand(help = "Information flow analysis for choreographic programs") {
    override fun run() = Unit
}

fun main(args: Array<String>) {
    ChorflowCommand().subcommands(
        CheckCommand(),
        PprintCommand(),
        GraphCommand()
    ).main(args)
}
