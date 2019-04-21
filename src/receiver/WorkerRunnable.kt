package receiver

import java.io.IOException
import java.net.Socket

/**
 *
 */
class WorkerRunnable(clientSocket: Socket, serverText: String) : Runnable {

    protected var clientSocket: Socket? = null
    protected var serverText: String? = null

    init {
        this.clientSocket = clientSocket
        this.serverText = serverText
    }

    override fun run() {
        try {
            val input = clientSocket!!.getInputStream()
            val output = clientSocket!!.getOutputStream()
            val time = System.currentTimeMillis()
            output.write(
                ("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
                        this.serverText + " - " +
                        time +
                        "").toByteArray()
            )
            output.close()
            input.close()
            println("Request processed: $time")
        } catch (e: IOException) {
            //report exception somewhere.
            e.printStackTrace()
        }

    }
}