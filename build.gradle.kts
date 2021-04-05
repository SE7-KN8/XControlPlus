import org.gradle.kotlin.dsl.support.compileKotlinScriptModuleTo
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    id("application")
    id("org.beryx.jlink") version "2.23.5"
}

group = "com.github.se7_kn8"
version = "0.0.0"

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

repositories {
    mavenCentral()
}

dependencies {
    // we can't use the plugin, see https://github.com/openjfx/javafx-gradle-plugin/issues/89
    implementation(project("gridview"))
    implementation("org.openjfx:javafx-base:16:${platform}")
    implementation("org.openjfx:javafx-controls:16:${platform}")
    implementation("org.openjfx:javafx-graphics:16:${platform}")

    // kotlinx.serialization is currently not compatible with JPMS so we use gson
    implementation("com.google.code.gson:gson:2.8.6")
}

java {
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("xcontrolplus")
    mainClass.set("com.github.se7_kn8.xcontrolplus.MainKt")
}

// Workaround: Java compiler won't compile empty modules
val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
        useIR = true
    }
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "XControlPlus"
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}
