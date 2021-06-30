package com.github.se7_kn8.xcontrolplus.app.project.turnout.logic

import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout
import com.github.se7_kn8.xcontrolplus.app.util.trace


abstract class TurnoutLogic<OutputState : Enum<*>>(val turnout: Turnout<OutputState>) {

    abstract val possibleInputs: Int
    abstract val possibleOutputs: Array<OutputState>
    abstract val defaultState: OutputState

    private var state = 0

    fun updateState(input: Int, active: Boolean) {
        state = if (active) { // Set bit
            state or (1 shl input)
        } else { // Clear bit
            state and ((1 shl input).inv())
        }
        trace { "Setting input $input to $active (New state is ${getState()})" }
    }

    fun getState(): OutputState {
        return turnout.stateMap.getOrDefault(state, defaultState)
    }

    fun getInputStatesFor(desiredState: OutputState): Map<Int, Boolean> {
        return turnout.stateMap.entries.firstOrNull { it.value == desiredState }?.let {
            val inputState = it.key
            val outputMap = HashMap<Int, Boolean>()
            for (bitPos in 0 until possibleInputs) {
                var value = false

                if (((inputState shr bitPos) and 0x1) == 0x1) {
                    value = true
                }

                outputMap[bitPos] = value
            }

            return outputMap
        } ?: emptyMap()
    }

    abstract fun getDefaultStateMap(): Map<Int, OutputState>
}
