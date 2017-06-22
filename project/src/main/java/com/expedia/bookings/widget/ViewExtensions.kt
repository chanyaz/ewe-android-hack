package com.expedia.bookings.widget

import android.view.View
import android.view.ViewTreeObserver

fun View.runWhenSizeAvailable(body: () -> Unit) {
    if (this.height > 0) {
        body()
    }
    else {
        this.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                this@runWhenSizeAvailable.viewTreeObserver.removeOnPreDrawListener(this)
                body()
                return true
            }
        })
    }
}
