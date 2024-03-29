package chorflow.util

inline fun <T> Iterable<T>.forEach(action: (T) -> Unit, doBetween: () -> Unit) {
    this.forEachIndexed { index, t ->
        if (index > 0)
            doBetween()
        action(t)
    }
}

fun Boolean.toInt() = if (this) 1 else 0
