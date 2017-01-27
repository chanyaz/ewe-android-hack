package com.expedia.vm.itin

import android.app.Activity
import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase

class ItinSignInViewModel(val context: Context) {
    var signInClickSubject = endlessObserver<Unit> {
        val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, ItineraryLoaderLoginExtender())
        User.signIn(context as Activity, args)
    }

    fun getSignInText(): String {
        return Phrase.from(context, R.string.Sign_in_with_TEMPLATE)
                .putOptional("brand", BuildConfig.brand)
                .format().toString()
    }

    fun getSignInContentDescription(): String {
        return Phrase.from(context, R.string.Sign_in_with_cont_desc_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format().toString()
    }
}