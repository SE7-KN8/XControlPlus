package com.github.se7_kn8.xcontrolplus.app.project.turnout.logic

import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout

enum class CrossingTurnoutOutputState {
    LEFT_TO_RIGHT,
    TOP_TO_BOTTOM,
    LEFT_TO_BOTTOM,
    TOP_TO_RIGHT;

    fun next(): CrossingTurnoutOutputState {
        return when (this) {
            LEFT_TO_RIGHT -> TOP_TO_BOTTOM
            TOP_TO_BOTTOM -> LEFT_TO_BOTTOM
            LEFT_TO_BOTTOM -> TOP_TO_RIGHT
            TOP_TO_RIGHT -> LEFT_TO_RIGHT
        }
    }
}

class CrossingTurnoutLogic(turnout: Turnout<CrossingTurnoutOutputState>) : TurnoutLogic<CrossingTurnoutOutputState>(turnout) {

    override val possibleInputs = 2

    override val possibleOutputs = CrossingTurnoutOutputState.values()

    override val defaultState = CrossingTurnoutOutputState.LEFT_TO_RIGHT

    override fun getDefaultStateMap() = mapOf(
        Pair(0, CrossingTurnoutOutputState.LEFT_TO_RIGHT),
        Pair(1, CrossingTurnoutOutputState.LEFT_TO_BOTTOM),
        Pair(2, CrossingTurnoutOutputState.TOP_TO_RIGHT),
        Pair(3, CrossingTurnoutOutputState.TOP_TO_BOTTOM)
    )
}
