package chorflow.ast

import chorflow.grammar.ChorBaseVisitor
import chorflow.grammar.ChorParser

class ASTVisitor : ChorBaseVisitor<ASTNode>() {

    override fun visitProgram(ctx: ChorParser.ProgramContext): Program {
        return Program(
            ctx.procedureList().procedure().map { it.accept(this) as Procedure },
            ctx.choreography()?.accept(this) as Choreography,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitProcedure(ctx: ChorParser.ProcedureContext): Procedure {
        return Procedure(
            ctx.ID().text,
            ctx.processList().ID().map { it.text },
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

    override fun visitAssignment(ctx: ChorParser.AssignmentContext): Assignment {
        return Assignment(
            ctx.ID(0).text,
            ctx.ID(1).text,
            ctx.expression().accept(this) as Expression,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitConditional(ctx: ChorParser.ConditionalContext): Conditional {
        return Conditional(
            ctx.ID().text,
            ctx.expression().accept(this) as Expression,
            ctx.choreography(0).accept(this) as Choreography,
            ctx.choreography(1).accept(this) as Choreography,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitInteraction(ctx: ChorParser.InteractionContext): Interaction {
        return Interaction(
            ctx.ID(0).text,
            ctx.expression().accept(this) as Expression,
            ctx.ID(1).text,
            ctx.ID(2).text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitSelection(ctx: ChorParser.SelectionContext): Selection {
        return Selection(
            ctx.ID(0).text,
            ctx.ID(1).text,
            ctx.ID(2).text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitProcedureCall(ctx: ChorParser.ProcedureCallContext): ProcedureCall {
        return ProcedureCall(
            ctx.ID().text,
            ctx.processList().ID().map { it.text },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitExpression(ctx: ChorParser.ExpressionContext): Expression {
        // TODO handle expressions
        return Expression(
            ctx.text,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }
}