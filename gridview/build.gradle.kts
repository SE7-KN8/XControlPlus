plugins {
    id("org.openjfx.javafxplugin")
}


java {
    modularity.inferModulePath.set(true)
}

javafx {
    version = "16"
    modules = listOf("javafx.graphics")
}
