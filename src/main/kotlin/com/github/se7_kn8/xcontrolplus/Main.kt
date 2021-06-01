package com.github.se7_kn8.xcontrolplus

import com.github.se7_kn8.xcontrolplus.app.XControlPlus
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.info
import javafx.application.Application
import java.util.*

fun main(args: Array<String>) {
    if (System.getProperty("java.util.logging.config.file") == null) {
        val mainClass = Class.forName("com.github.se7_kn8.xcontrolplus.MainKt")
        val file = mainClass.getResource("/assets/logging/logging.properties")!!.file
        System.setProperty("java.util.logging.config.file", file)
        info("Set logging.properties to $file")
    }

    info("Starting XControlPlus")
    if (System.getProperty("lang")?.isNotBlank() == true) {
        val newLocale = Locale.forLanguageTag(System.getProperty("lang"))
        info("Setting locale to $newLocale")
        Locale.setDefault(newLocale)
    }
    System.setProperty("javafx.preloader", "com.github.se7_kn8.xcontrolplus.Preloader")
    Application.launch(XControlPlus::class.java, *args)
    debug("Stopping main thread")
}
