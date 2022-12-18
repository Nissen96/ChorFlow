package chorflow.flow

import chorflow.util.toInt
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.MutableGraph
import java.io.File
import javax.swing.*

class FlowGraph(flow: Flow = Flow(), private val policy: Flow? = null) {
    var graph: MutableGraph

    init {
        // If policy is not specified, all flow is ok
        val (okFlow, badFlow) = if (policy == null) {
            flow.flows to emptyList()
        } else {
            flow.flows.partition { f -> policy.let { f in it.flows } }
        }
        val policyFlow = policy?.flows?.minus(okFlow.toSet()) ?: emptyList()
        val nFlowTypes = okFlow.isNotEmpty().toInt() + badFlow.isNotEmpty().toInt() + policyFlow.isNotEmpty().toInt()

        graph = graph(directed = true) {
            if (nFlowTypes <= 1)
                graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), GraphAttr.CONCENTRATE]
            else
                graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT)]

            badFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.SOLID, Color.RED] }
            okFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.SOLID, Color.BLACK] }
            policyFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.DASHED, Color.BLACK] }
        }
    }

    fun display() {
        object : JFrame("flowGraph") {
            init {
                val label = JLabel("", ImageIcon(graph.toGraphviz().height(1000).render(Format.SVG).toImage()), 0)
                add(label)
                pack()
                defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                isVisible = true
            }
        }
    }

    fun save(file: File) {
        graph.toGraphviz().height(1000).render(Format.PNG).toFile(file)
    }
}