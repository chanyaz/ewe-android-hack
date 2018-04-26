package com.expedia.bookings.itin.utils

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import com.expedia.bookings.R

class ActivityLauncher(val context: Context) : IActivityLauncher {
    override fun launchActivity(intentable: Intentable, id: String) {
        context.startActivity(intentable.createIntent(context, id), animationBundle)
    }

    private val animationBundle = ActivityOptionsCompat
            .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete)
            .toBundle()
}
