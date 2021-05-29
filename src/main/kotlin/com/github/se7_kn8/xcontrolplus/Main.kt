package com.github.se7_kn8.xcontrolplus

import com.github.se7_kn8.xcontrolplus.app.XControlPlus
import javafx.application.Application
import java.util.*

fun main(args: Array<String>) {
    if (System.getProperty("lang")?.isNotBlank() == true) {
        Locale.setDefault(Locale.forLanguageTag(System.getProperty("lang")))
    }
    Application.launch(XControlPlus::class.java, *args)
}
