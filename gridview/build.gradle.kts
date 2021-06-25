plugins {
    java
}

java {
    modularity.inferModulePath.set(true)
}

val currentOs = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()!!
var platform = when {
    currentOs.isWindows -> {
        "win"
    }
    currentOs.isLinux -> {
        "linux"
    }
    currentOs.isMacOsX -> {
        "mac"
    }
    else -> {
        "UNKNOWN_PLATFORM"
    }
}

dependencies {
    compileOnly("org.openjfx:javafx-base:16:$platform")
    compileOnly("org.openjfx:javafx-graphics:16:$platform")
}
