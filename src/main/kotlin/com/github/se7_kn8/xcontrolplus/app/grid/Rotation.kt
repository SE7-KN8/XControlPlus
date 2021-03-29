package com.github.se7_kn8.xcontrolplus.app.grid

enum class Rotation(val degree: Double) {
    D0(0.0) {
        override fun next() = D90
    },
    D90(90.0) {
        override fun next() = D180
    },
    D180(180.0) {
        override fun next() = D270
    },
    D270(270.0) {
        override fun next() = D0
    }, ;

    abstract fun next(): Rotation
}
