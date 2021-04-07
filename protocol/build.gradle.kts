plugins {
    java
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    implementation("com.fazecast:jSerialComm:2.6.2")
}
