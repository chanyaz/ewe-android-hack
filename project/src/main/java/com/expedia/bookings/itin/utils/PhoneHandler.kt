package com.expedia.bookings.itin.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.utils.ClipboardUtils
import com.mobiata.android.SocialUtils

class PhoneHandler(val context: Context, val packageManager: PackageManager = context.packageManager) : IPhoneHandler {
    override fun handle(phoneNumber: String) {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            SocialUtils.call(context, phoneNumber)
        } else {
            ClipboardUtils.setText(context, phoneNumber)
            Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }
}

interface IPhoneHandler {
    fun handle(phoneNumber: String)
}
