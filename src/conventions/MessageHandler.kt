package conventions

import java.io.BufferedWriter

enum class MessageHandler(val side: Side, val handle: (Message) -> Unit = {}) {

    // // CLIENT SIDE // //

    CHECK_ADDRESS(Side.SERVER, {
        it.answer(ADDRESS_IS_AVAILABLE, it.priority.toString())
    }),

    RECEIVE_CONSOLE_OUT(Side.SERVER),

    RECEIVE_CONSOLE_ERR(Side.SERVER),

    // // SERVER SIDE // //

    ADDRESS_IS_AVAILABLE(Side.CLIENT, {
        val priority = Integer.valueOf(it.arguments[0])
    }),

    RECEIVE_FILE(Side.CLIENT)

    ;

    companion object {
        private val map = HashMap<String, MessageHandler>().also {
            MessageHandler.values().forEach { i ->
                it[i.name] = i
            }
        }

        operator fun get(string: String): MessageHandler? = map[string]
    }

    data class Message(
        val bufferedWriter: BufferedWriter,
        val arguments: List<String>,
        val side: Side,
        val path: String,
        val priority: Int
    ) {
        fun answer(message: MessageHandler, arguments: List<String>) {
            val list = listOf(message.name) + arguments
            bufferedWriter.appendln(list.toSplitedByVerticalBarString())
        }
        fun answer(message: MessageHandler, vararg arguments: String) = answer(message, arguments.toList())
    }
}