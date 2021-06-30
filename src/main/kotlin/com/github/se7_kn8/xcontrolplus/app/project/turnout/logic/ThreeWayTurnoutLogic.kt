package com.github.se7_kn8.xcontrolplus.app.project.turnout.logic

import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout

enum class ThreeWayTurnoutOutputState {
    NOT_TURNED,
    TURNED_LEFT,
    TURNED_RIGHT;

    fun next(): ThreeWayTurnoutOutputState {
        return when (this) {
            NOT_TURNED -> TURNED_LEFT
            TURNED_LEFT -> TURNED_RIGHT
            TURNED_RIGHT -> NOT_TURNED
        }
    }
}

class ThreeWayTurnoutLogic(turnout: Turnout<ThreeWayTurnoutOutputState>) : TurnoutLogic<ThreeWayTurnoutOutputState>(turnout) {

    override val possibleInputs = 2

    override val possibleOutputs = ThreeWayTurnoutOutputState.values()

    override val defaultState: ThreeWayTurnoutOutputState = ThreeWayTurnoutOutputState.NOT_TURNED

    override fun getDefaultStateMap() = mapOf(
        Pair(0, ThreeWayTurnoutOutputState.NOT_TURNED),
        Pair(1, ThreeWayTurnoutOutputState.TURNED_LEFT),
        Pair(2, ThreeWayTurnoutOutputState.TURNED_RIGHT),
        Pair(3, ThreeWayTurnoutOutputState.NOT_TURNED)
    )
}
