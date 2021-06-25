package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ConnectionChoiceDialog
import com.github.se7_kn8.xcontrolplus.app.grid.TurnoutGridCell
import com.github.se7_kn8.xcontrolplus.app.project.Sheet
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.info
import com.github.se7_kn8.xcontrolplus.app.util.trace
import com.github.se7_kn8.xcontrolplus.app.util.warn
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import java.util.function.Consumer

class ConnectionHandler : Consumer<Packet> {


    inner class CheckIfConnectionStillAlive : Runnable {

        private val checkIntervall: Long = 5000

        var receivedEcho: Int = Int.MAX_VALUE // The echo packet can never reach this value

        override fun run() {
            threadLoop@ while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(checkIntervall)
                    try {
                        val conn = connection.get()
                        if (conn != null) {
                            if (conn.isOpen) {
                                receivedEcho = Int.MAX_VALUE
                                trace { "Testing connection" }
                                val echoPacket = EchoPacket()
                                conn.sendPacket(echoPacket)
                                val start = System.currentTimeMillis()
                                var receivedAnswer = false
                                while ((System.currentTimeMillis() - start) < 5000 && !receivedAnswer) {
                                    try {
                                        Thread.sleep(50) // Save some cpu time
                                    } catch (e: InterruptedException) {
                                        break@threadLoop
                                    }
                                    if (receivedEcho == echoPacket.randomNumber.toInt()) {
                                        receivedAnswer = true
                                    }
                                }
                                if (!receivedAnswer) {
                                    Platform.runLater { onTimeout() }
                                }
                            } else {
                                Platform.runLater { onClosed() }
                            }
                        }
                    } catch (e: Exception) {
                        warn(e, "Problem while testing connection")
                        Platform.runLater { onTimeout() }
                    }
                } catch (e: InterruptedException) {
                    break@threadLoop
                }
            }
        }

    }

    val connection = SimpleObjectProperty<Connection?>()

    private val turnoutMap = HashMap<Int, ArrayList<Consumer<Boolean>>>()
    private val connectionTesterRunnable = CheckIfConnectionStillAlive()
    private val connectionTestThread = Thread(connectionTesterRunnable).apply {
        name = "ConnectionKeepAliveTest"
    }

    init {
        connectionTestThread.start()

        Packet.registerPacket(PacketIDs.TURNOUT_PACKET, TurnoutPacket.TurnoutPacketFactory())
        connection.addListener { _, oldValue, newValue ->
            oldValue?.let {
                it.closeConnection()
                debug("Closing old connection")
            }
            newValue?.let {
                info("New connection ${it.name}")
                it.openConnection()
                it.setOnPacketReceived(this)
            }
        }
    }


    fun showConnectionSelectDialog() {
        connection.value = ConnectionChoiceDialog().showDialog()
    }

    fun hasConnection() = connection.get() != null

    private fun onTimeout() {
        info("Connection encountered timeout")
        closeConnection()
        Alert(Alert.AlertType.ERROR, "Connection timed out. Closing connection").apply { initOwner(WindowContext.get().primaryStage) }.show()
    }

    private fun onClosed() {
        info("Connection was closed")
        closeConnection()
        Alert(Alert.AlertType.ERROR, "Connection has been closed. Closing local connection").apply { initOwner(WindowContext.get().primaryStage) }
            .show()
    }

    private fun closeConnection() {
        debug("Closing connection")
        connection.get()?.closeConnection()
        connection.set(null)
    }

    fun addTurnout(id: Int, onTurn: Consumer<Boolean>) {
        debug("Register turnout consumer $onTurn with id $id")
        turnoutMap.getOrPut(id) { ArrayList() }.add(onTurn)
    }

    fun removeTurnout(id: Int, onTurn: Consumer<Boolean>) {
        debug("Remove turnout consumer $onTurn with id $id")
        turnoutMap.getOrPut(id) { ArrayList() }.remove(onTurn)
    }

    fun close() {
        connectionTestThread.interrupt()
        closeConnection()
    }

    fun removeSheet(sheet: Sheet) {
        sheet.gridHelper.getCells().filterIsInstance<TurnoutGridCell>().forEach { removeTurnout(it.id.get(), it) }
    }

    fun clear() {
        debug("Clearing turnout map")
        turnoutMap.clear()
    }

    override fun accept(packet: Packet) {
        debug("Received packet: $packet")
        when (packet) {
            is EchoPacket -> {
                connectionTesterRunnable.receivedEcho = packet.randomNumber.toInt()
            }
            is TurnoutPacket -> {
                Platform.runLater { turnoutMap[packet.address]?.forEach { it.accept(packet.isTurned()) } }
            }
        }
    }

    fun sendPacket(packet: Packet) {
        debug("Try to send packet: $packet")
        connection.get()?.sendPacket(packet)
    }

}
