package receiver

import conventions.MessageHandler
import conventions.Side
import conventions.SyncAndRun
import conventions.splitByVerticalBar
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.net.Socket

/**
 *
 */
class WorkerRunnable(
    private val clientSocket: Socket,
    private val serverText: String) : Runnable {

    override fun run() {
        try {
            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()
            val bufferedReader = BufferedReader(input.reader(SyncAndRun.CHARSET))
            val bufferedWriter = BufferedWriter(output.writer(SyncAndRun.CHARSET))
            while (clientSocket.isConnected) {
                val line = bufferedReader.readLine()
                val lines = line.splitByVerticalBar()
                val message = MessageHandler[lines.first()]
                if (message != null && message.side == Side.SERVER) {
                    val arguments = lines.subList(1, lines.lastIndex)
                    val msg = MessageHandler.Message(
                        bufferedWriter,
                        arguments,
                        Side.SERVER,
                        Receiver.receivingPath,
                        Receiver.priority
                    )
                    message.handle(msg)
                }
            }
            output.close()
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}