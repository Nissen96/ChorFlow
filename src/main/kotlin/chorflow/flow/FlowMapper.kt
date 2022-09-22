package chorflow.flow

import chorflow.ast.Assignment
import chorflow.ast.Conditional
import chorflow.ast.Interaction
import chorflow.ast.Selection

class FlowMapper {
    fun flows(assignment: Assignment): Flow {
        var flow = Flow(
            assignment.expression.variables.map { Pair(it, assignment.variable) }.toSet()
        )
        flow += Flow(setOf(Pair(assignment.process, assignment.variable)))
        return flow
    }

    fun flows(conditional: Conditional): Flow {
        return Flow(
            conditional.expression.variables.map { Pair(it, conditional.process) }.toSet()
        )
    }

    fun flows(interaction: Interaction): Flow {
        var flow = Flow(
            interaction.expression.variables.map { Pair(it, interaction.destinationVariable) }.toSet()
        )
        flow += Flow(setOf(
            Pair(interaction.sourceProcess, interaction.destinationProcess),
            Pair(interaction.destinationProcess, interaction.sourceProcess),
            Pair(interaction.destinationProcess, interaction.destinationVariable)
        ))
        return flow
    }

    fun flows(selection: Selection): Flow {
        return Flow(setOf(
            Pair(selection.sourceProcess, selection.destinationProcess),
            Pair(selection.destinationProcess, selection.sourceProcess)
        ))
    }
}