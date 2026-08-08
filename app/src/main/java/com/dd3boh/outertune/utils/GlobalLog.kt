package com.dd3boh.outertune.utils

import android.util.Log

object GlobalLog {
    fun append(priority: Int, tag: String, message: String, throwable: Throwable? = null) {
        when (priority) {
            Log.VERBOSE -> Log.v(tag, message, throwable)
            Log.DEBUG -> Log.d(tag, message, throwable)
            Log.INFO -> Log.i(tag, message, throwable)
            Log.WARN -> Log.w(tag, message, throwable)
            Log.ERROR -> Log.e(tag, message, throwable)
            else -> Log.d(tag, message, throwable)
        }
    }
}
