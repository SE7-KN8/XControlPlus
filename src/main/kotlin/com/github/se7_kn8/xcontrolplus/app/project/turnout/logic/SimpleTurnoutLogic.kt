package com.github.se7_kn8.xcontrolplus.app.project.turnout.logic

import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout

enum class SimpleTurnoutOutputState {
    NOT_TURNED,
    TURNED;

    fun next(): SimpleTurnoutOutputState {
        if (this == TURNED) {
            return NOT_TURNED
        }
        return TURNED
    }

}

class SimpleTurnoutLogic(turnout: Turnout<SimpleTurnoutOutputState>) : TurnoutLogic<SimpleTurnoutOutputState>(turnout) {

    // Only one input
    override val possibleInputs = 1

    override val possibleOutputs = SimpleTurnoutOutputState.values()

    override val defaultState = SimpleTurnoutOutputState.NOT_TURNED

    override fun getDefaultStateMap() = mapOf(
        Pair(0, SimpleTurnoutOutputState.NOT_TURNED),
        Pair(1, SimpleTurnoutOutputState.TURNED)
    )
}
