package chorflow.visitor

import chorflow.ast.*

class SubstitutionVisitor(processParameters: List<String>, processArguments: List<String>): Visitor() {
    private val substitutions = processParameters.zip(processArguments).toMap()

    override fun visit(process: Process) {
        process.id = substitutions[process.id]!!
    }
}