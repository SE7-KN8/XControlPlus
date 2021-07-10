import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.hasProperty
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    kotlin("jvm") version "1.5.10"
    id("application")
    id("org.javamodularity.moduleplugin") version "1.8.7"
    id("org.beryx.jlink") version "2.23.5"
}

group = "com.github.se7_kn8"
version = "0.0.0"
// TODO change this for release versions
val versionInfo = "snapshot"

val currentOs = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()!!
val arch = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentArchitecture()!!
val debugFeatures = System.getProperties().hasProperty("xcontrolplus.debug")

println("OS: $currentOs")
println("Arch: $arch")

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
    implementation(project("gridview"))
    implementation(project("protocol"))
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.slf4j:slf4j-jdk14:1.7.30")
    implementation("org.jfxtras:jmetro:11.6.15")


    val javaFXVersion = "17-ea+14"

    if (arch.isAmd64 || arch.isI386) {
        implementation("org.openjfx:javafx-base:$javaFXVersion:${platform}")
        implementation("org.openjfx:javafx-controls:$javaFXVersion:${platform}")
        implementation("org.openjfx:javafx-graphics:$javaFXVersion:${platform}")
    } else {
        println("Excluded javafx runtime dependencies")
        compileOnly("org.openjfx:javafx-base:$javaFXVersion:${platform}")
        compileOnly("org.openjfx:javafx-controls:$javaFXVersion:${platform}")
        compileOnly("org.openjfx:javafx-graphics:$javaFXVersion:${platform}")
    }

}

java {
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("xcontrolplus")
    mainClass.set("com.github.se7_kn8.xcontrolplus.MainKt")
    applicationDefaultJvmArgs = listOf("-Dprism.verbose=true")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
    }
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "XControl Plus"
    }
    jpackage {
        imageName = "XControlPlus"
        installerOptions = listOf("--win-menu", "--win-shortcut", "--win-upgrade-uuid", "00570e5a-6922-4ebe-a474-2556b0d99b9e", "--win-dir-chooser")
        icon = "$projectDir/src/main/resources/assets/logo/large.ico"
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("createBuildMetadata") {
    dependsOn("processResources")
    doLast {
        File("$buildDir/resources/main/build.properties").writer().use {
            val properties = Properties()
            properties["name"] = project.name
            properties["version"] = project.version.toString()
            properties["timestamp"] = Date().toString()
            properties["commit"] = getCurrentCommitHash()
            properties["versionInfo"] = versionInfo
            properties["debug"] = debugFeatures.toString()

            properties.store(it, "Build properties")
        }
    }
}

tasks["classes"].dependsOn("createBuildMetadata")

fun getCurrentCommitHash(): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = output
    }
    return output.toString().trim()
}
