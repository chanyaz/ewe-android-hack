package com.expedia.bookings.itin.utils

import android.content.Context

class ActivityLauncher(val context: Context, val intentable: Intentable, val id: String) : IActivityLauncher {
    override fun launchActivity() {
        context.startActivity(intentable.createIntent(context, id))
    }
}
