package org.phdeh.syncandrun.receiver

import java.util.*


object Receiver {
    var receivingPath: String = ""
    var priority: Int = 0
    private val receiverBytes = ByteArray(64)
    val receiverId = let { Random().nextBytes(receiverBytes); String(receiverBytes) }

    operator fun invoke(receivingPath: String, priority: Int) {
        Receiver.receivingPath = receivingPath
        Receiver.priority = priority

        Thread(MultiThreadedServer()).run()
    }
}
