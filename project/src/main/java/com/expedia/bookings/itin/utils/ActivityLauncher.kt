package com.expedia.bookings.itin.utils

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import com.expedia.bookings.R

class ActivityLauncher(val context: Context) : IActivityLauncher {
    override fun launchActivity(intentable: Intentable, id: String, animationDirection: AnimationDirection) {
        when (animationDirection) {
            AnimationDirection.SLIDE_RIGHT -> {
                context.startActivity(intentable.createIntent(context, id), slideRightAnimation)
            }
            AnimationDirection.SLIDE_UP -> {
                context.startActivity(intentable.createIntent(context, id), slideUpAnimation)
            }
        }
    }

    val slideRightAnimation = ActivityOptionsCompat
            .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete)
            .toBundle()

    val slideUpAnimation = ActivityOptionsCompat
            .makeCustomAnimation(context, R.anim.slide_up_partially, R.anim.slide_down_partially)
            .toBundle()
}
