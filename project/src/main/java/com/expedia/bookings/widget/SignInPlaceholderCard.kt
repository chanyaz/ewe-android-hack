package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.NavUtils

class SignInPlaceholderCard(itemView: View, context: Context) : AbstractGenericPlaceholderCard(itemView, context, R.drawable.plus_pattern) {

    val activity = context as Activity

    init {
        button_one.setOnClickListener {
            NavUtils.goToSignIn(context, false)
            OmnitureTracking.trackLaunchSignIn()
        }

        button_two.setOnClickListener {
            NavUtils.goToAccount(activity)
            OmnitureTracking.trackLaunchSignIn()
        }
    }

}