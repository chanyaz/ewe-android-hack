package com.expedia.bookings.itin.utils

import android.content.Context

class ActivityLauncher(val context: Context) : IActivityLauncher {
    override fun launchActivity(intentable: Intentable, id: String) {
        context.startActivity(intentable.createIntent(context, id))
    }
}
