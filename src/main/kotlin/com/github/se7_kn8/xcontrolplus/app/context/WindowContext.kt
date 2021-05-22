package com.github.se7_kn8.xcontrolplus.app.context

import javafx.stage.Stage

class WindowContext(val primaryStage: Stage) {
    companion object {

        private lateinit var instance: WindowContext
        private var init = false;

        fun init(primaryStage: Stage) {
            if (!init) {
                init = true
                instance = WindowContext(primaryStage)
            }
        }

        fun get() = instance
    }
}
