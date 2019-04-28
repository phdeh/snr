package conventions

internal val protectedCharacters = mapOf(
    'b' to '\b',
    'n' to '\n',
    't' to '\t',
    'r' to '\r',
    '|' to '|'
)

internal val unprotectedCharacters = protectedCharacters.reversed()

fun String.splitByVerticalBar(): List<String> {
    val ml = mutableListOf<String>()

    val sb = StringBuilder()
    var protect = false
    for (i in 0..(this.length - 1)) {
        val c = this[i]
        if (protect) {
            val pc = protectedCharacters[c]
            if (pc != null)
                sb.append(pc)
            else
                sb.append(c)
            protect = false
        } else when (c) {
            '\\' -> protect = true
            '|' -> {
                ml += sb.toString()
                sb.setLength(0)
            }
            else -> sb.append(c)
        }
    }
    ml += sb.toString()

    return ml.toList()
}

fun List<String>.toSplitedByVerticalBarString(): String {
    val sb = StringBuilder()
    var first = true
    this.forEach {
        if (first)
            first = false
        else
            sb.append('|')
        for (i in 0..(it.length - 1)) {
            val c = it[i]
            val uc = unprotectedCharacters[c]
            if (uc != null) {
                sb.append('\\')
                sb.append(uc)
            } else
                sb.append(c)
        }
    }
    return sb.toString()
}

fun <K, V> Map<K, V>.reversed() = HashMap<V, K>().also { newMap ->
    entries.forEach { newMap.put(it.value, it.key) }
}