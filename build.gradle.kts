import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "com.github.se7_kn8"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


javafx {
    version = "15.0.1"
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "15"
    kotlinOptions.useIR = true
}

