package chorflow.flow

import chorflow.ast.Assignment
import chorflow.ast.Conditional
import chorflow.ast.Interaction
import chorflow.ast.Selection

class FlowMapper(private val mappingSpec: MappingSpec) {
    fun flows(assignment: Assignment): Flow {
        val p = assignment.process.id
        val x = assignment.variable
        val e = assignment.expression.variables
        println(p)
        println(x)
        println(e)

        return Flow(
            mappingSpec.assignment.map { f ->
                when (f) {
                    Pair("p", "x") -> listOf(p to x)
                    Pair("x", "p") -> listOf(x to p)
                    Pair("p", "e") -> e.map { p to it }
                    Pair("e", "p") -> e.map { it to p }
                    Pair("x", "e") -> e.map { x to it }
                    Pair("e", "x") -> e.map { it to x }
                    else -> emptyList()
                }
            }.flatten().toSet()
        )
    }

    fun flows(conditional: Conditional): Flow {
        val p = conditional.process.id
        val e = conditional.expression.variables

        return Flow(
            mappingSpec.conditional.map { f ->
                when (f) {
                    Pair("p", "e") -> e.map { p to it }
                    Pair("e", "p") -> e.map { it to p }
                    else -> emptyList()
                }
            }.flatten().toSet()
        )
    }

    fun flows(interaction: Interaction): Flow {
        val p = interaction.sourceProcess.id
        val e = interaction.expression.variables
        val q = interaction.destinationProcess.id
        val x = interaction.destinationVariable

        return Flow(
            mappingSpec.assignment.map { f ->
                when (f) {
                    Pair("p", "e") -> e.map { p to it }
                    Pair("p", "q") -> listOf(p to q)
                    Pair("p", "x") -> listOf(p to x)
                    Pair("e", "p") -> e.map { it to p }
                    Pair("e", "q") -> e.map { it to q }
                    Pair("e", "x") -> e.map { it to x }
                    Pair("q", "p") -> listOf(q to p)
                    Pair("q", "e") -> e.map { q to it }
                    Pair("q", "x") -> listOf(q to x)
                    Pair("x", "p") -> listOf(x to p)
                    Pair("x", "q") -> listOf(x to q)
                    Pair("x", "e") -> e.map { x to it }
                    else -> emptyList()
                }
            }.flatten().toSet()
        )
    }

    fun flows(selection: Selection): Flow {
        val p = selection.sourceProcess.id
        val q = selection.destinationProcess.id

        return Flow(
            mappingSpec.selection.mapNotNull {
                when (it) {
                    Pair("p", "q") -> p to q
                    Pair("q", "p") -> q to p
                    else -> null
                }
            }.toSet()
        )
    }
}
