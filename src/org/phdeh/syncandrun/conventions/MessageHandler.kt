package org.phdeh.syncandrun.conventions

import com.google.common.util.concurrent.SimpleTimeLimiter
import org.apache.commons.io.FileUtils
import org.phdeh.syncandrun.receiver.Receiver
import org.phdeh.syncandrun.sender.Sender
import java.io.*
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

enum class MessageHandler(val side: Side, val args: Int = 0, val handle: (Message) -> Unit = {}) {

    // // CLIENT SIDE // //

    CHECK_ADDRESS(Side.SERVER, 2, {
        if (it.arguments[1] == SyncAndRun.key)
            it.answer(
                ADDRESS_IS_AVAILABLE,
                it.priority.toString(),
                Receiver.receiverId
            )
        else
            it.socket.close()
    }),

    CLEAN_UP(Side.SERVER, 0, {
        FileUtils.cleanDirectory(File(it.path))
    }),

    EXECUTE(Side.SERVER, 1, {
        thread {
            val process = Runtime.getRuntime().exec(it.arguments[0], null, File(it.path))
            var stop = false
            fun scan(print: (String) -> Unit, outputStream: InputStream, message: MessageHandler): Thread {
                return thread {
                    val sc = BufferedReader(outputStream.reader())
                    val limiter = SimpleTimeLimiter()
                    while (!stop) {
                        limiter.callWithTimeout({
                            val next = sc.readLine()
                            if (next != null) {
                                println(next)
                                it.answer(message, next)
                            }
                        }, SyncAndRun.timeout.toLong(), TimeUnit.MILLISECONDS, false)
                    }
                }
            }

            scan(System.out::println, process.inputStream, CONSOLE_OUT)
            scan(System.err::println, process.errorStream, CONSOLE_ERR)

            while (it.socket.isConnected && process.isAlive) {
                Thread.sleep(SyncAndRun.timeout.toLong())
            }
            Thread.sleep(SyncAndRun.timeout.toLong())
            stop = true
            process.destroyForcibly()
            it.socket.close()
        }
    }),

    CLOSE(Side.SERVER, 0, {
        it.socket.close()
    }),

    RECEIVE_FILE(Side.SERVER, 2, {
        val path = Path.of(it.path, it.arguments[0])
        val value = it.arguments[1]
        path.toFile().parentFile.mkdirs()
        Files.writeString(path, value)
    }),

    // // SERVER SIDE // //

    CONSOLE_OUT(Side.CLIENT, 1, {
        println(it.arguments[0])
    }),

    CONSOLE_ERR(Side.CLIENT, 1, {
        System.err.println(it.arguments[0])
    }),

    ADDRESS_IS_AVAILABLE(Side.CLIENT, 2, {
        val priority = Integer.valueOf(it.arguments[0])
        val receiverId = it.arguments[1]
        it.socket.soTimeout = 0
        Sender.check(it, priority, receiverId)
    }),

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
        val socket: Socket,
        val priority: Int
    ) {
        fun answer(message: MessageHandler, arguments: List<String>) {
            val list = listOf(message.name) + arguments
            bufferedWriter.append(list.toSplittedByVerticalBarString())
            bufferedWriter.newLine()
            bufferedWriter.flush()
        }

        fun answer(message: MessageHandler, vararg arguments: String) = answer(message, arguments.toList())
    }
}