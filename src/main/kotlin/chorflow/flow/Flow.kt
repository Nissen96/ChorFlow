package chorflow.flow

class Flow(val flows: Set<Pair<String, String>> = emptySet()) {
    operator fun plus(flow: Flow): Flow {
        return Flow(flows + flow.flows)
    }

    operator fun plus(flow: Set<Pair<String, String>>): Flow {
        return Flow(flows + flow)
    }

    operator fun plus(flow: Pair<String, String>): Flow {
        return Flow(flows + setOf(flow))
    }

    operator fun contains(flow: Pair<String, String>): Boolean {
        return flow in flows
    }
}
