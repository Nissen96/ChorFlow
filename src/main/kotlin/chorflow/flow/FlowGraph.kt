package chorflow.flow

import chorflow.util.toInt
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.MutableGraph
import java.io.File
import javax.swing.*

class FlowGraph(flow: Flow, private val policy: Flow?) {
    var graph: MutableGraph

    init {
        val (okFlow, badFlow) = flow.flows.partition { f -> policy?.let { f in it.flows } ?: true }
        val policyFlow = policy?.flows?.minus(okFlow.toSet())
        val nFlowTypes = okFlow.isNotEmpty().toInt() + badFlow.isNotEmpty().toInt() + (policyFlow?.isNotEmpty()?.toInt() ?: 0)

        graph = graph(directed = true) {
            if (nFlowTypes <= 1)
                graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), GraphAttr.CONCENTRATE]
            else
                graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT)]

            badFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.SOLID, Color.RED] }
            okFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.SOLID, Color.BLACK] }
            policyFlow?.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.DASHED, Color.BLACK] }
        }
    }

    fun display() {
        object : JFrame("flowGraph") {
            init {
                val label = JLabel("", ImageIcon(graph.toGraphviz().height(500).render(Format.SVG).toImage()), 0)
                add(label)
                pack()
                defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                isVisible = true
            }
        }
    }

    fun save(filename: String) {
        graph.toGraphviz().height(500).render(Format.PNG).toFile(File(filename))
    }
}