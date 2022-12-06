package chorflow

import chorflow.ast.ASTVisitor
import chorflow.ast.Program
import chorflow.flow.*
import chorflow.grammar.ChorLexer
import chorflow.grammar.ChorParser
import chorflow.visitor.PrettyPrintVisitor
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
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

class ChorFlowArgs(parser: ArgParser) {
    val chor by parser.storing("-c", "--chor", help = "Path to choreography").default(null)
    val pol by parser.storing("-p", "--pol", help = "Path to policy").default(null)
    val flowmap by parser.storing("-m", "--map", help = "Path to flow mapping").default(null)
    val validate by parser.flagging("-v", "--validate", help = "Validate flow based on policy")
    val pprint by parser.flagging("--pprint", help = "Pretty print choreography (default: false)").default(false)
    val display by parser.storing("-d", "--display", help = "Display graph visualization [chor/pol/both] (default: disabled)").default(null).addValidator {
        if (value != null && value !in setOf("chor", "pol", "both")) {
            throw InvalidArgumentException("Display value must be either 'chor' for choreography, 'pol' for policy, or 'both' for a combined view")
        }
    }
    val save by parser.storing("-s", "--save", help = "Save graph visualization [chor/pol/both] (default: disabled)").default(null).addValidator {
        if (value != null && value !in setOf("chor", "pol", "both")) {
            throw InvalidArgumentException("Save value must be either 'chor' for choreography, 'pol' for policy, or 'both' for a combined view")
        }
    }
    val output by parser.storing("-o", "--out", help = "Output directory for saved graph images (default: current folder)").default(".")
}

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::ChorFlowArgs).run {
        val dir = output.removeSuffix("/")

        // Parse policies if provided and optionally generate and display/save their flow graphs
        val (policy, localPolicies) = pol?.let { parsePolicyFile(it) } ?: Pair(Flow(), emptyMap())
        if (display == "pol" || save == "pol") {
            val policies = localPolicies + ("global" to policy)
            val policyGraphs = policies.entries.associate { (name, flow) -> name to FlowGraph(policy = flow) }
            if (display == "pol")
                policyGraphs.values.forEach { it.display() }
            if (save == "pol")
                policyGraphs.forEach { (name, graph) -> graph.save("${dir}/${name}-policy.png") }
        }

        // Basic validation
        if (chor == null || pol == null || flowmap == null) {
            if (validate) {
                throw InvalidArgumentException("You must specify the path to both a choreography, policy, and mapping spec to validate")
            }

            // At this point we can stop if no choreography is provided
            if (chor == null) {
                if (pprint) {
                    throw InvalidArgumentException("You must specify a choreography path to pretty print it")
                }
                return
            }
        }

        // Parse choreography and optionally print it
        val program = parseChoreographyFile(chor!!)

        if (pprint) {
            program.accept(PrettyPrintVisitor(indentation = 4, condensed = false))
            println()
        }

        // Parse flow mapping specification if specified and generate mapping function
        val mappingSpec = flowmap?.let { parseMappingFile(it) } ?: MappingSpec()
        val flowMapper = FlowMapper(mappingSpec)

        // Run type checking to collect flow and flow violations
        val typeChecker = TypeChecker(flowMapper, localPolicies)
        val (flow, localFlows) = typeChecker.checkFlow(program, policy)

        // Display/save flow graphs if chosen
        if (display == "chor" || save == "chor") {
            val choreographies = localFlows + ("main" to flow)
            val chorGraphs = choreographies.entries.associate { (name, flow) -> name to FlowGraph(flow = flow) }
            if (display == "chor")
                chorGraphs.values.forEach { it.display() }
            if (save == "chor")
                chorGraphs.forEach { (name, graph) -> graph.save("${dir}/${name}-flow.png") }
        }
        if (display == "both" || save == "both") {
            val flowGraph = FlowGraph(flow = flow, policy = policy)
            val localFlowGraphs = localFlows.entries.associate {
                    (name, localFlow) -> name to FlowGraph(flow = localFlow, policy = localPolicies[name]!!)
            }
            if (display == "both") {
                flowGraph.display()
                localFlowGraphs.values.forEach { it.display() }
            }
            if (save == "both") {
                (localFlowGraphs + ("main" to flowGraph)).forEach { (name, graph) -> graph.save("${dir}/${name}-flow.png") }
            }
        }

        // If validating, generate error message from flow violations found
        if (validate) {
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
}
