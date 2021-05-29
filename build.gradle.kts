import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.23.5"
}

group = "com.github.se7_kn8"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project("gridview"))
    implementation(project("protocol"))
    implementation("com.google.code.gson:gson:2.8.6")
}

javafx {
    version = "16"
    modules = listOf("javafx.controls")
}


java {
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("xcontrolplus")
    mainClass.set("com.github.se7_kn8.xcontrolplus.MainKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
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
