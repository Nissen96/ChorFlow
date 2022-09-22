package chorflow.flow

class Flow(val flows: Set<Pair<String, String>> = emptySet()) {
    operator fun plus(flow: Flow): Flow {
        return Flow(flows + flow.flows)
    }

    operator fun contains(flow: Pair<String, String>): Boolean {
        return flow in flows
    }
}
