package chorflow.ast

abstract class Action(lineNumber: Int, charPosition: Int) : Instruction(lineNumber, charPosition), Event
