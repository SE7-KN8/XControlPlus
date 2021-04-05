plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}


java {
    modularity.inferModulePath.set(true)
}

javafx {
    version = "16"
    modules = listOf("javafx.graphics")
}
