package org.phdeh.syncandrun.sender

import org.phdeh.syncandrun.conventions.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.lang.Exception
import java.net.ConnectException
import java.net.InetAddress
import kotlin.concurrent.thread
import java.net.*
import java.util.*

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
            val myThread = thread {
                val lh = lhBytes.clone()
                lh[lh.lastIndex] = it.toByte()
                val address = InetAddress.getByAddress(lh)
                val socket = Socket()
                try {
                    socket.connect(InetSocketAddress(address, SyncAndRun.DEFAULT_PORT), timeout)
                } catch (e: SocketTimeoutException) {
                    return@thread
                } catch (e: ConnectException) {
                    return@thread
                }
                val bufferedWriter = BufferedWriter(socket.getOutputStream().writer(SyncAndRun.CHARSET))
                bufferedWriter.append(
                    listOf(

                        MessageHandler.CHECK_ADDRESS.name,

                        Sender.priority.toString(),

                        SyncAndRun.key

                    ).toSplittedByVerticalBarString()
                )
                bufferedWriter.newLine()
                bufferedWriter.flush()
                val bufferedReader = BufferedReader(socket.getInputStream().reader(SyncAndRun.CHARSET))
                while (!socket.isClosed) {
                    try {
                        val line = bufferedReader.readLine()
                        if (line != null) {
                            val lines = line.splitByVerticalBar()
                            val message = MessageHandler[lines.first()]
                            socket.soTimeout = timeout
                            if (message != null && message.side.isClient && lines.size - 1 == message.args) {
                                val arguments = lines.subList(1, lines.size)
                                val msg = MessageHandler.Message(
                                    bufferedWriter,
                                    arguments,
                                    Side.CLIENT,
                                    Sender.sendingPath,
                                    socket,
                                    Sender.priority
                                )
                                message.handle(msg)
                            } else if (message != null) {
                                System.err.println("Client waited for $message(${message.args}), but ${lines.size - 1} args found")
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        if (SyncAndRun.unquiet)
                            e.printStackTrace()
                        CrashCode.EXECUTION_COMPLETE.crash()
                    } catch (e: Exception) {
                        if (SyncAndRun.unquiet)
                                e.printStackTrace()
                        return@thread
                    }
                }
            }
            myThread
        }
    }
    t.forEach { it.join() }
    continueReceive = false
    Thread.sleep(timeout / 2L)
    val l = ml.toList()
    return List(l.size) { l[it].address }
}
