package chorflow.ast

import chorflow.grammar.ChorBaseVisitor
import chorflow.grammar.ChorParser

class ASTVisitor : ChorBaseVisitor<ASTNode>() {

    override fun visitProgram(ctx: ChorParser.ProgramContext): Program {
        return Program(
            ctx.procedureList().procedure().map { it.accept(this) as Procedure },
            ctx.choreography().accept(this) as Choreography,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitProcedure(ctx: ChorParser.ProcedureContext): Procedure {
        return Procedure(
            ctx.ID().text,
            ctx.processList().process().map { it.accept(this) as Process },
            ctx.choreography().accept(this) as Choreography,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitChoreography(ctx: ChorParser.ChoreographyContext): Choreography? {
        if (ctx.ZERO() != null)
            return null

        return Choreography(
            ctx.instruction().accept(this) as Instruction,
            ctx.choreography().accept(this) as Choreography?,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitInstruction(ctx: ChorParser.InstructionContext): Instruction {
        return when {
            ctx.instruction() != null -> ctx.instruction().accept(this)
            ctx.assignment() != null -> ctx.assignment().accept(this)
            ctx.conditional() != null -> ctx.conditional().accept(this)
            ctx.interaction() != null -> ctx.interaction().accept(this)
            ctx.selection() != null -> ctx.selection().accept(this)
            ctx.procedureCall() != null -> ctx.procedureCall().accept(this)
            else -> throw Exception("Invalid instruction type!")
        } as Instruction
    }

    override fun visitProcess(ctx: ChorParser.ProcessContext): Process {
        return Process(
            ctx.ID().text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitAssignment(ctx: ChorParser.AssignmentContext): Assignment {
        return Assignment(
            ctx.process().accept(this) as Process,
            ctx.ID().text,
            ctx.expression().accept(this) as Expression,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitConditional(ctx: ChorParser.ConditionalContext): Conditional {
        return Conditional(
            ctx.process().accept(this) as Process,
            ctx.expression().accept(this) as Expression,
            ctx.choreography(0).accept(this) as Choreography,
            ctx.choreography(1).accept(this) as Choreography,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitInteraction(ctx: ChorParser.InteractionContext): Interaction {
        return Interaction(
            ctx.process(0).accept(this) as Process,
            ctx.expression().accept(this) as Expression,
            ctx.process(1).accept(this) as Process,
            ctx.ID().text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitSelection(ctx: ChorParser.SelectionContext): Selection {
        return Selection(
            ctx.process(0).accept(this) as Process,
            ctx.process(1).accept(this) as Process,
            ctx.ID().text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitProcedureCall(ctx: ChorParser.ProcedureCallContext): ProcedureCall {
        return ProcedureCall(
            ctx.ID().text,
            ctx.processList().process().map { it.accept(this) as Process },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitExpression(ctx: ChorParser.ExpressionContext): Expression {
        // No need to actually handle expressions, we are only interested in the variables for the flow
        val variables = mutableSetOf<String>()
        if (ctx.ID() != null && ctx.expression().isEmpty()) {
            variables.add(ctx.ID().text)
        }
        ctx.expression().forEach { variables.addAll((it.accept(this) as Expression).variables) }

        return Expression(
            ctx.text,
            variables,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }
}
