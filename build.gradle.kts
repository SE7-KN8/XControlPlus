import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "com.github.se7_kn8"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
}

javafx {
    version = "16"
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
}

java {
    modularity.inferModulePath.set(true)
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
    kotlinOptions.useIR = true
}

