package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ConnectionChoiceDialog
import com.github.se7_kn8.xcontrolplus.app.project.Sheet
import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.info
import com.github.se7_kn8.xcontrolplus.app.util.trace
import com.github.se7_kn8.xcontrolplus.app.util.warn
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import com.github.se7_kn8.xcontrolplus.protocol.Connections
import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import java.util.function.BiConsumer
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
    val hasConnection = SimpleBooleanProperty(false)
    val trackStop = SimpleBooleanProperty(false)

    private val turnoutMap = HashMap<Int, MutableSet<BiConsumer<Int, Boolean>>>()
    private val connectionTesterRunnable = CheckIfConnectionStillAlive()
    private val connectionTestThread = Thread(connectionTesterRunnable).apply {
        name = "ConnectionKeepAliveTest"
    }

    init {
        connectionTestThread.start()

        Packet.registerPacket(PacketIDs.TURNOUT_PACKET, TurnoutPacket.TurnoutPacketFactory())
        Packet.registerPacket(PacketIDs.TRACK_POWER_PACKET, TrackPowerPacket.TrackPowerPacketFactory())


        connection.addListener { _, oldValue, newValue ->
            oldValue?.let {
                it.closeConnection()
                debug("Closing old connection")
            }
            newValue?.let {
                info("New connection ${it.simpleName}")
                it.openConnection()
                it.setOnPacketReceived(this)
            }
            hasConnection.set(newValue != null)
        }
    }


    fun showConnectionSelectDialog() {
        connection.value = ConnectionChoiceDialog().showDialog()
    }

    fun hasConnection() = hasConnection.get()

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


    fun updateTurnout(oldAddress: Int, turnout: Turnout<*>) {
        removeTurnout(oldAddress, turnout)
        addTurnout(turnout)
    }


    fun addTurnout(turnout: Turnout<*>) {
        debug { "Register turnout consumer $turnout for addresses ${turnout.getAddresses().joinToString { it.toString() }}" }
        turnout.getAddresses().forEach {
            turnoutMap.getOrPut(it) { HashSet() }.add(turnout)
        }
        trace { "New turnout map is $turnoutMap" }
    }

    fun removeTurnout(oldAddress: Int, turnout: Turnout<*>) {
        debug { "Remove turnout consumer $turnout for address $oldAddress" }
        turnoutMap.getOrPut(oldAddress) { HashSet() }.remove(turnout)
        trace { "New turnout map is $turnoutMap" }
    }

    fun removeTurnout(turnout: Turnout<*>) {
        debug { "Remove turnout consumer $turnout for addresses ${turnout.getAddresses().joinToString { it.toString() }}" }
        turnout.getAddresses().forEach {
            turnoutMap.getOrPut(it) { HashSet() }.remove(turnout)
        }
        trace { "New turnout map is $turnoutMap" }
    }

    fun close() {
        connectionTestThread.interrupt()
        closeConnection()
    }

    fun removeSheet(sheet: Sheet) {
        sheet.gridHelper.getCells().filterIsInstance(Turnout::class.java).forEach(::removeTurnout)
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
                Platform.runLater { turnoutMap[packet.address]?.forEach { it.accept(packet.address, packet.isTurned()) } }
            }
            is TrackPowerPacket -> {
                Platform.runLater { trackStop.set(packet.state != 1) } // XNET_TRACK_POWER_NORMAL
            }
        }
    }

    fun sendPacket(packet: Packet) {
        debug("Try to send packet: $packet")
        connection.get()?.sendPacket(packet)
    }

    fun loadLatestConnection() {
        val latestConnection = ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_CONNECTION]
        val split = latestConnection.split(":")
        val typeName = split[0]
        val connName = split[1]
        val conn =
            Connections.getTypes().filter { it.simpleName == typeName }.firstOrNull()?.listConnections()?.filter { it.simpleName == connName }
                ?.firstOrNull()
        conn?.let {
            info("Loading connection ${it.fullName}")
            it.openConnection()
            val valid = it.testConnection(5000)
            it.closeConnection()
            Platform.runLater {
                if (valid) {
                    connection.set(conn)
                } else {
                    warn("Could not load connection on start")
                }
            }
        }

    }

}
