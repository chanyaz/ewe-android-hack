package com.expedia.bookings.shared

import android.os.SystemClock
import android.view.View

class DebounceOnClickListener(private val handler: (v: View?) -> Unit, private val debounceTimeout: Long = 500L) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View?) {
        val elapsedTime = SystemClock.elapsedRealtime()
        if (elapsedTime - lastClickTime < debounceTimeout) {
            return
        }
        lastClickTime = elapsedTime
        handler(v)
    }
}
