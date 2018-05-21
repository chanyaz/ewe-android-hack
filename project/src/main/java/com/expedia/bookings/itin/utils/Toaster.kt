package com.expedia.bookings.itin.utils

import android.content.Context
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.utils.ClipboardUtils

class Toaster(val context: Context) : IToaster {
    override fun toastAndCopy(message: CharSequence) {
        if (message.isNotEmpty()) {
            ClipboardUtils.setText(context, message)
            Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }
}

interface IToaster {
    fun toastAndCopy(message: CharSequence)
}
