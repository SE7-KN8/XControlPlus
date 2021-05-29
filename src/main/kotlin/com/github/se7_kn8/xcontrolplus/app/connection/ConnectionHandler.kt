package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.dialog.ConnectionChoiceDialog
import com.github.se7_kn8.xcontrolplus.app.grid.TurnoutGridCell
import com.github.se7_kn8.xcontrolplus.app.project.Sheet
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
            oldValue?.closeConnection()
            newValue?.openConnection()
            newValue?.setOnPacketReceived(this)
        }
    }

    fun showConnectionSelectDialog() {
        connection.value = ConnectionChoiceDialog().showDialog()
    }

    fun hasConnection() = connection.get() != null

    fun addTurnout(id: Int, onTurn: Consumer<Boolean>) {
        turnoutMap.getOrPut(id) { ArrayList() }.add(onTurn)
    }

    fun removeTurnout(id: Int, onTurn: Consumer<Boolean>) {
        turnoutMap.getOrPut(id) { ArrayList() }.remove(onTurn)
    }

    fun closeConnection() {
        connection.get()?.closeConnection()
    }

    fun removeSheet(sheet: Sheet) {
        sheet.gridHelper.getCells().filterIsInstance<TurnoutGridCell>().forEach { removeTurnout(it.id.get(), it.onPacket) }
    }

    fun clear() {
        turnoutMap.clear()
    }

    override fun accept(packet: Packet) {
        when (packet) {
            is TurnoutPacket -> {
                Platform.runLater { turnoutMap[packet.turnoutId]?.forEach { it.accept(packet.turned) } }
            }
        }
    }


}
