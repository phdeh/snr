import conventions.*
import receiver.Receiver
import sender.Sender
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.InetAddress
import kotlin.concurrent.thread
import java.net.*
import java.util.*
import java.lang.System.out
import java.net.DatagramPacket


fun listNetworkAddresses(): List<InetAddress> {
    val ml = mutableListOf<InetAddress>()
    val nets = NetworkInterface.getNetworkInterfaces()
    for (netint in Collections.list(nets))
        ml.addAll(netint.inetAddresses.toList())
    return ml.toList()
}

fun listAddresses(
    timeout: Int = 5000,
    networks: List<InetAddress> = listNetworkAddresses()
): List<InetAddress> {
    data class Session(val address: InetAddress, val sessionKey: String) {
        override fun hashCode() = sessionKey.hashCode()
        override fun equals(other: Any?) = other is Session && other.sessionKey == sessionKey
    }

    val ml = mutableSetOf<Session>()
    val t = mutableListOf<Thread>()
    var continueReceive = true
    networks.forEach { network ->
        t += List(256) {
            val lhBytes = network.address
            thread {
                val lh = lhBytes.clone()
                lh[lh.lastIndex] = it.toByte()
                val address = InetAddress.getByAddress(lh)
                val socket = Socket()
                socket.connect(InetSocketAddress(address, SyncAndRun.DEFAULT_PORT), timeout)
                val bufferedWriter = BufferedWriter(socket.getOutputStream().writer(SyncAndRun.CHARSET))
                bufferedWriter.appendln(
                    listOf(

                        MessageHandler.CHECK_ADDRESS.name,

                        Sender.priority.toString()

                    ).toSplitedByVerticalBarString()
                )
                val bufferedReader = BufferedReader(socket.getInputStream().reader(SyncAndRun.CHARSET))
                while (socket.isConnected) {
                    val line = bufferedReader.readLine()
                    val lines = line.splitByVerticalBar()
                    val message = MessageHandler[lines.first()]
                    if (message != null && message.side == Side.CLIENT) {
                        val arguments = lines.subList(1, lines.lastIndex)
                        val msg = MessageHandler.Message(
                            bufferedWriter,
                            arguments,
                            Side.CLIENT,
                            Sender.sendingPath,
                            Sender.priority
                        )
                        message.handle(msg)
                    }
                }
            }
        }
    }
    t.forEach { it.join() }
    continueReceive = false
    Thread.sleep(timeout / 2L)
    val l = ml.toList()
    return List(l.size) { l[it].address }
}
