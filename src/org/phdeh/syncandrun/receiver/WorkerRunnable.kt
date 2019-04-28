package org.phdeh.syncandrun.receiver

import org.phdeh.syncandrun.conventions.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.lang.Exception
import java.net.Socket

class WorkerRunnable(
    private val clientSocket: Socket,
    private val serverText: String) : Runnable {

    override fun run() {
        try {
            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()
            val bufferedReader = BufferedReader(input.reader(SyncAndRun.CHARSET))
            val bufferedWriter = BufferedWriter(output.writer(SyncAndRun.CHARSET))
            while (!clientSocket.isClosed) {
                try {
                    val line = bufferedReader.readLine()
                    if (line != null) {
                        val lines = line.splitByVerticalBar()
                        val message = MessageHandler[lines.first()]
                        if (message != null && message.side.isServer && lines.size - 1 == message.args) {
                            val arguments = lines.subList(1, lines.size)
                            val msg = MessageHandler.Message(
                                bufferedWriter,
                                arguments,
                                Side.SERVER,
                                Receiver.receivingPath,
                                clientSocket,
                                Receiver.priority
                            )
                            message.handle(msg)
                        } else if (message != null) {
                            System.err.println("Server waited for $message(${message.args}), but ${lines.size - 1} args found")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    output.close()
                    input.close()
                    clientSocket.close()
                }
            }
            output.close()
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}