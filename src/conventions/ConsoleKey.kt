package conventions

enum class ConsoleKey(val args: Int, val help: String = "") {

    HELP(0, " --  prints this message"),
    RECEIVE(1, "<PATH>  --  starts receiving server in path"),
    SEND(1, "<PATH>  --  sends path to server"),
    PRIORITY(1, "<PRIORITY>  --  sets server or client priority")

    ;

    val shortKey = "-${name.first().toLowerCase()}"
    val longKey = "--${name.toLowerCase()}"

    companion object {
        private fun findConsoleKey(str: String): ConsoleKey? {
            values().forEach {
                if (str == it.longKey || str == it.shortKey)
                    return it
            }
            return null
        }

        fun from(arguments: Array<String>): Map<ConsoleKey, List<String>> {
            val map = mutableMapOf<ConsoleKey, List<String>>()
            var key: ConsoleKey? = null
            val args = mutableListOf<String>()
            arguments.forEach {
                val k = key
                if (k == null) {
                    val currKey = findConsoleKey(it)
                    if (currKey != null) {
                        if (currKey.args == 0)
                            map += currKey to listOf()
                        else
                            key = currKey
                    } else {
                        System.err.println("Wrong key \"$it\", use ${HELP.longKey}.")
                        CrashCode.BAD_CONSOLE_KEYS.crash()
                    }
                } else {
                    args += it
                    if (args.size == k.args) {
                        map += k to args.toList()
                        key = null
                        args.clear()
                    }
                }
            }
            val k = key
            if (args.size != k?.args ?: 0) {
                System.err.println("Argument number mismatch for key ${k?.longKey}")
                CrashCode.ARGUMENT_NUMBER_MISMATCH
            }
            return map.toMap()
        }
    }
}