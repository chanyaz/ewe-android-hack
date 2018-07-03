package com.expedia.layouttestandroid.util

import android.os.Handler
import android.os.Looper

object LayoutTestUtils {

    fun isUiThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun runOnUiThread(block: () -> Unit) {
        var e: Exception? = null
        val handler = Handler(Looper.getMainLooper())
        val lock = Object()

        synchronized(lock) {
            handler.post {
                try {
                    block()
                } catch (ee: Exception) {
                    e = ee
                }
                synchronized(lock) {
                    lock.notifyAll()
                }
            }
            try {
                lock.wait()
            } catch (ee: InterruptedException) {
                throw RuntimeException(ee)
            }
        }

        e?.let {
            throw RuntimeException(e)
        }
    }
}
