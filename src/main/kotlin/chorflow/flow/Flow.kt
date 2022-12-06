package chorflow.flow

class Flow(val flows: MutableSet<Pair<String, String>> = mutableSetOf()) {
    operator fun plus(flow: Flow): Flow {
        return Flow((flows + flow.flows).toMutableSet())
    }

    operator fun contains(flow: Pair<String, String>): Boolean {
        return flow in flows
    }

    fun add(flow: Pair<String, String>) {
        flows.add(flow)
    }

    fun isSubflow(other: Flow): Boolean {
        return flows.all { it in other.flows }
    }
}
