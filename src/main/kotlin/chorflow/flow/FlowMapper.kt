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
        flow += Flow(setOf(Pair(assignment.process.id, assignment.variable)))
        return flow
    }

    fun flows(conditional: Conditional): Flow {
        return Flow(
            conditional.expression.variables.map { Pair(it, conditional.process.id) }.toSet()
        )
    }

    fun flows(interaction: Interaction): Flow {
        var flow = Flow(
            interaction.expression.variables.map { Pair(it, interaction.destinationVariable) }.toSet()
        )
        flow += Flow(setOf(
            Pair(interaction.sourceProcess.id, interaction.destinationProcess.id),
            Pair(interaction.destinationProcess.id, interaction.sourceProcess.id),
            Pair(interaction.destinationProcess.id, interaction.destinationVariable)
        ))
        return flow
    }

    fun flows(selection: Selection): Flow {
        return Flow(setOf(
            Pair(selection.sourceProcess.id, selection.destinationProcess.id),
            Pair(selection.destinationProcess.id, selection.sourceProcess.id)
        ))
    }
}