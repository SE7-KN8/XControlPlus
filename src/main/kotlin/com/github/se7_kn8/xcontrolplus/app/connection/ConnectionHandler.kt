package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.dialog.ConnectionChoiceDialog
import com.github.se7_kn8.xcontrolplus.app.grid.TurnoutGridCell
import com.github.se7_kn8.xcontrolplus.app.project.Sheet
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.info
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import java.util.function.Consumer

class ConnectionHandler : Consumer<Packet> {

    val connection = SimpleObjectProperty<Connection?>()
    val turnoutMap = HashMap<Int, ArrayList<Consumer<Boolean>>>()

    init {
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

    fun addTurnout(id: Int, onTurn: Consumer<Boolean>) {
        debug("Register turnout consumer $onTurn with id $id")
        turnoutMap.getOrPut(id) { ArrayList() }.add(onTurn)
    }

    fun removeTurnout(id: Int, onTurn: Consumer<Boolean>) {
        debug("Remove turnout consumer $onTurn with id $id")
        turnoutMap.getOrPut(id) { ArrayList() }.remove(onTurn)
    }

    fun closeConnection() {
        debug("Closing connection")
        connection.get()?.closeConnection()
    }

    fun removeSheet(sheet: Sheet) {
        sheet.gridHelper.getCells().filterIsInstance<TurnoutGridCell>().forEach { removeTurnout(it.id.get(), it) }
    }

    fun clear() {
        debug("Clearing turnout map")
        turnoutMap.clear()
    }

    override fun accept(packet: Packet) {
        debug("New packet: $packet")
        when (packet) {
            is TurnoutPacket -> {
                Platform.runLater { turnoutMap[packet.address]?.forEach { it.accept(packet.isTurned()) } }
            }
        }
    }


}
