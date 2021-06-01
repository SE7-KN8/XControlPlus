package com.github.se7_kn8.xcontrolplus.app.util

import mu.KLogger
import mu.KotlinLogging

private object Logging {
    val loggers = HashMap<String, KLogger>()

    fun getCallerName(): String = Thread.currentThread().stackTrace[4].className

    fun getLogger(): KLogger {
        val name = getCallerName()
        return loggers.getOrPut(name) {
            return KotlinLogging.logger(Class.forName(name).simpleName)
        }
    }
}

fun trace(msg: () -> Any?) {
    Logging.getLogger().trace(msg)
}

fun trace(msg: String) {
    Logging.getLogger().trace(msg)
}

fun debug(msg: () -> Any?) {
    Logging.getLogger().debug(msg)
}

fun debug(msg: String) {
    Logging.getLogger().debug(msg)
}

fun info(msg: () -> Any?) {
    Logging.getLogger().info(msg)
}

fun info(msg: String) {
    Logging.getLogger().info(msg)
}

fun warn(msg: () -> Any?) {
    Logging.getLogger().warn(msg)
}

fun warn(msg: String) {
    Logging.getLogger().warn(msg)
}

fun warn(throwable: Throwable, msg: () -> Any?) {
    Logging.getLogger().warn(throwable, msg)
}

fun warn(throwable: Throwable, msg: String) {
    Logging.getLogger().warn(msg, throwable)
}

fun error(msg: () -> Any?) {
    Logging.getLogger().error(msg)
}

fun error(msg: String) {
    Logging.getLogger().error(msg)
}

fun error(throwable: Throwable, msg: () -> Any?) {
    Logging.getLogger().error(throwable, msg)
}

fun error(throwable: Throwable, msg: String) {
    Logging.getLogger().error(msg, throwable)
}
