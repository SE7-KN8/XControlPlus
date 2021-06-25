package com.github.se7_kn8.xcontrolplus.app.util

import java.util.*

class BuildInfo {

    val properties = Properties()

    init {
        properties.load(FileUtil.getResourceAsStream("build.properties"))
    }

    fun getVersion(): String {
        return properties.getProperty("version")
    }

    fun getVersionInfo(): String {
        return properties.getProperty("versionInfo")
    }

    fun getTimestamp(): String {
        return properties.getProperty("timestamp")
    }

    fun getName(): String {
        return properties.getProperty("name")
    }

    fun getCommit(): String {
        return properties.getProperty("commit")
    }

}
