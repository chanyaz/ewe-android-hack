package com.expedia.bookings.itin.utils

import android.content.Context
import android.content.Intent

interface Intentable {
    fun createIntent(context: Context, id: String): Intent
}

interface IntentableWithType {
    fun createIntent(context: Context, id: String, type: String): Intent
}
