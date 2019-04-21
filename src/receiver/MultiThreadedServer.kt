package receiver

import conventions.SyncAndRun
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class MultiThreadedServer(private val serverPort: Int = SyncAndRun.DEFAULT_PORT) : Runnable {

    private var serverSocket: ServerSocket? = null
    private var isStopped = false
    protected var runningThread: Thread? = null

    override fun run() {
        synchronized(this) {
            this.runningThread = Thread.currentThread()
        }
        openServerSocket()
        while (!isStopped) {
            var clientSocket: Socket? = null
            try {
                clientSocket = this.serverSocket!!.accept()
            } catch (e: IOException) {
                if (isStopped) {
                    println("Server Stopped.")
                    return
                }
                throw RuntimeException(
                    "Error accepting client connection", e
                )
            }

            Thread(
                WorkerRunnable(
                    clientSocket, "Multithreaded Server"
                )
            ).start()
        }
        println("Server Stopped.")
    }

    @Synchronized
    fun stop() {
        this.isStopped = true
        try {
            this.serverSocket!!.close()
        } catch (e: IOException) {
            throw RuntimeException("Error closing server", e)
        }

    }

    private fun openServerSocket() {
        try {
            this.serverSocket = ServerSocket(this.serverPort)
        } catch (e: IOException) {
            throw RuntimeException("Cannot open port $serverPort", e)
        }

    }

}