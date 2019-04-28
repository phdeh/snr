package receiver

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket


object Receiver {
    var receivingPath: String = ""
    var priority: Int = 0

    operator fun invoke(receivingPath: String, priority: Int) {
        this.receivingPath = receivingPath
        this.priority = priority
    }

    init {
        MultiThreadedServer()
    }
}
