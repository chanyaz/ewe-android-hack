package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.widget.AbstractGenericPlaceholderCard

class SignInPlaceholderCard(itemView: View, context: Context) : AbstractGenericPlaceholderCard(itemView, context) {

    val activity = context as Activity

    init {
        button_one.setOnClickListener {
            NavUtils.goToSignIn(context, false, true, 0)
            OmnitureTracking.trackLaunchSignIn()
        }

        button_two.setOnClickListener {
            NavUtils.goToAccount(activity)
            OmnitureTracking.trackLaunchSignIn()
        }
    }

}