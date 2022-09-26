package chorflow.flow

data class MappingSpec(
    val assignment: MutableList<Pair<String, String>> = mutableListOf(),
    val conditional: MutableList<Pair<String, String>> = mutableListOf(),
    val selection: MutableList<Pair<String, String>> = mutableListOf(),
    val interaction: MutableList<Pair<String, String>> = mutableListOf()
)
