package com.expedia.bookings.extensions

import android.os.Bundle

fun Bundle.safePutString(key: String, value: String?) {
    if (value != null) {
        this.putString(key, value)
    } else {
        this.putString(key, "")
    }
}

fun Bundle.safePutInt(key: String, value: Int?) {
    if (value != null) {
        this.putInt(key, value)
    } else {
        this.putInt(key, 0)
    }
}
