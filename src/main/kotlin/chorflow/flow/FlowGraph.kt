package chorflow.flow

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.GraphAttr.SplineMode
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.MutableGraph
import java.io.File
import javax.swing.*

class FlowGraph(flow: Flow, private val policy: Flow?) {
    var graph: MutableGraph

    init {
        val (okFlow, badFlow) = flow.flows.partition { f -> policy?.let { f in it.flows } ?: true }
        val policyFlow = policy?.flows?.minus(okFlow.toSet())

        graph = graph(directed = true) {
            graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), GraphAttr.CONCENTRATE]

            policyFlow?.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Style.DASHED] }
            okFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE] }
            badFlow.forEach { (src, dst) -> (src - dst)[Arrow.VEE, Color.RED] }
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