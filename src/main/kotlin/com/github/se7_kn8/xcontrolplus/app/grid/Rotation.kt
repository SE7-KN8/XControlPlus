package com.github.se7_kn8.xcontrolplus.app.grid

enum class Rotation {
    D0 {
        override fun next() = D90
    },
    D90 {
        override fun next() = D180
    },
    D180 {
        override fun next() = D270
    },
    D270 {
        override fun next() = D0
    }, ;

    abstract fun next(): Rotation
}
