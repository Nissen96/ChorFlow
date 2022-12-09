package chorflow.flow

import chorflow.ast.*

class FlowMapper(private val mappingSpec: MappingSpec) {
    fun flow(event: Event): Flow {
        return when (event) {
            is Assignment -> flow(event)
            is Guard -> flow(event)
            is Selection -> flow(event)
            is Interaction -> flow(event)
            else -> throw IllegalArgumentException("flow mapping function is not defined for this event type")
        }
    }

    private fun flow(assignment: Assignment): Flow {
        val p = assignment.process.id
        val x = assignment.variable
        val e = assignment.expression.variables

        return Flow(
            mappingSpec.assignment.map { f ->
                when (f) {
                    Pair("p", "x") -> listOf(p to "$p.$x")
                    Pair("x", "p") -> listOf("$p.$x" to p)
                    Pair("p", "e") -> e.map { p to "$p.$it" }
                    Pair("e", "p") -> e.map { "$p.$it" to p }
                    Pair("x", "e") -> e.map { "$p.$x" to "$p.$it" }
                    Pair("e", "x") -> e.map { "$p.$it" to "$p.$x" }
                    else -> emptyList()
                }
            }.flatten().toMutableSet()
        )
    }

    private fun flow(guard: Guard): Flow {
        val p = guard.process.id
        val e = guard.expression.variables

        return Flow(
            mappingSpec.conditional.map { f ->
                when (f) {
                    Pair("p", "e") -> e.map { p to "$p.$it" }
                    Pair("e", "p") -> e.map { "$p.$it" to p }
                    else -> emptyList()
                }
            }.flatten().toMutableSet()
        )
    }

    private fun flow(interaction: Interaction): Flow {
        val p = interaction.sourceProcess.id
        val e = interaction.expression.variables
        val q = interaction.destinationProcess.id
        val x = interaction.destinationVariable

        return Flow(
            mappingSpec.interaction.map { f ->
                when (f) {
                    Pair("p", "e") -> e.map { p to "$p.$it" }
                    Pair("p", "q") -> listOf(p to q)
                    Pair("p", "x") -> listOf(p to "$q.$x")
                    Pair("e", "p") -> e.map { "$p.$it" to p }
                    Pair("e", "q") -> e.map { "$p.$it" to q }
                    Pair("e", "x") -> e.map { "$p.$it" to "$q.$x" }
                    Pair("q", "p") -> listOf(q to p)
                    Pair("q", "e") -> e.map { q to "$p.$it" }
                    Pair("q", "x") -> listOf(q to "$q.$x")
                    Pair("x", "p") -> listOf("$q.$x" to p)
                    Pair("x", "q") -> listOf("$q.$x" to q)
                    Pair("x", "e") -> e.map { "$q.$x" to "$p.$it" }
                    else -> emptyList()
                }
            }.flatten().toMutableSet()
        )
    }

    private fun flow(selection: Selection): Flow {
        val p = selection.sourceProcess.id
        val q = selection.destinationProcess.id

        return Flow(
            mappingSpec.selection.mapNotNull {
                when (it) {
                    Pair("p", "q") -> p to q
                    Pair("q", "p") -> q to p
                    else -> null
                }
            }.toMutableSet()
        )
    }
}
