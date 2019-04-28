package org.phdeh.syncandrun.sender

import org.phdeh.syncandrun.conventions.MessageHandler
import org.phdeh.syncandrun.conventions.SyncAndRun
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

object Sender {
    var sendingPath: String = ""
    var priority: Int = 0
    private val queue = ConcurrentLinkedQueue<SenderHandle>()

    operator fun invoke(sendingPath: String, priority: Int) {
        Sender.sendingPath = sendingPath
        Sender.priority = priority
        thread { listAddresses(SyncAndRun.timeout) }
        Thread.sleep(SyncAndRun.timeout.toLong())
        val sender = queue.peek()
        if (sender != null) {
            if (SyncAndRun.unquiet)
                println("[Connected to ${sender.message.socket.inetAddress}:${sender.message.socket.port}]")
            sender.message.answer(MessageHandler.CLEAN_UP)
            Thread.sleep(SyncAndRun.timeout.toLong())
            val path = Paths.get(sendingPath)
            Files.walk(path)
                .filter { Files.isRegularFile(it) }
                .forEach {
                    val str = String(Files.readAllBytes(it), SyncAndRun.CHARSET)
                    sender.message.answer(MessageHandler.RECEIVE_FILE, path.relativize(it).toString(), str)
                    if (SyncAndRun.unquiet)
                        println("[Sent \"${path.relativize(it)}\"]")
                }
            Thread.sleep(SyncAndRun.timeout.toLong())
            val exec = SyncAndRun.execute
            if (exec != null) {
                if (SyncAndRun.unquiet)
                    println("[Executing \"$exec\"]")
                sender.message.answer(MessageHandler.EXECUTE, exec)
            } else {
                if (SyncAndRun.unquiet)
                    println("[Closing connection]")
                sender.message.answer(MessageHandler.CLOSE)
            }
        }
    }

    private data class SenderHandle(
        val message: MessageHandler.Message,
        val priority: Int,
        val id: String
    ) {
        @Volatile
        private var open: Boolean = true

        fun close() {
            if (open) {
                open = false
                message.socket.close()
            }
        }
    }

    fun check(message: MessageHandler.Message, priority: Int, id: String) {
        queue.forEach {
            if (it.id == id)
                return@check
            else if (it.priority >= priority)
                return@check
            else if (it.priority < priority)
                it.close()
        }
        val senderHandle = SenderHandle(message, priority, id)
        queue += senderHandle
    }
}
