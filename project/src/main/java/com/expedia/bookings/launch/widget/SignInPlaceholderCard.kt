package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.support.annotation.VisibleForTesting
import android.view.View
import com.expedia.account.Config
import com.expedia.account.NewAccountView
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.isNewSignInEnabled
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.AbstractGenericPlaceholderCard

open class SignInPlaceholderCard(itemView: View, context: Context) : AbstractGenericPlaceholderCard(itemView, context) {

    val activity = context as Activity

    init {
        button_one.setOnClickListener {
            NavUtils.goToSignIn(context, false, true, 0)
            OmnitureTracking.trackLaunchSignIn()
        }

        button_two.setOnClickListener {
            if (isNewSignInEnabled(context)) {
                goToNewCreateAccountPage()
            } else {
                goToOldCreateAccountPage()
            }
            OmnitureTracking.trackLaunchSignIn()
        }
    }

    @VisibleForTesting
    protected open fun goToNewCreateAccountPage() {
        NavUtils.goToAccount(activity, NewAccountView.AccountTab.CREATE_ACCOUNT)
    }

    @VisibleForTesting
    protected open fun goToOldCreateAccountPage() {
        NavUtils.goToAccount(activity, Config.InitialState.SinglePageCreateAccount)
    }
}
