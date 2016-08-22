package com.expedia.bookings.widget.rail

import android.app.ProgressDialog
import android.content.Context

class CreateTripProgressDialog(context: Context) : ProgressDialog(context) {
    init {
        setCancelable(false)
        isIndeterminate = true
    }
}