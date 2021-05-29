package com.github.se7_kn8.xcontrolplus.app.util

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import java.util.*

class Translator {

    private val bundle = ResourceBundle.getBundle("assets.lang.lang", Locale.getDefault())

    operator fun get(key: String): String {
        return if (bundle.containsKey(key)) {
            bundle.getString(key)
        } else {
            key
        }
    }

    fun format(key: String, vararg args: Any): String {
        val text = this[key]
        return text.format(*args)
    }

}

fun translate(key: String, vararg args: Any) = ApplicationContext.get().translator.format(key, args)

fun translate(key: String) = ApplicationContext.get().translator[key]
