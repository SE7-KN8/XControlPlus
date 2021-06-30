package com.github.se7_kn8.xcontrolplus.app.project.turnout

import com.github.se7_kn8.xcontrolplus.app.connection.TurnoutPacket
import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.dialog.NoConnectionDialog
import com.github.se7_kn8.xcontrolplus.app.project.turnout.logic.TurnoutLogic
import java.util.function.BiConsumer


interface Turnout<Output : Enum<*>> : BiConsumer<Int, Boolean> {

    val logic: TurnoutLogic<Output>

    var stateMap: Map<Int, Output>

    fun init()

    fun getAddresses(): IntArray

    fun turnTo(newState: Output) {
        val conn = ApplicationContext.get().connectionHandler
        if (conn.hasConnection() || ApplicationContext.get().buildInfo.isDebug()) {
            val toSend = logic.getInputStatesFor(newState)
            toSend.forEach {
                conn.sendPacket(TurnoutPacket.newOperation(turnoutInputToAddress(it.key), it.value))
                Thread.sleep(10) // TODO this is not a good way to prevent overflow on the connection
            }
        } else {
            NoConnectionDialog().showDialog()
        }
    }

    override fun accept(address: Int, state: Boolean) {
        logic.updateState(addressToTurnoutInput(address), state)
    }

    fun turnoutInputToAddress(input: Int): Int

    fun addressToTurnoutInput(address: Int): Int
}
