package com.expedia.bookings.test.robolectric

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import org.robolectric.Robolectric

import org.robolectric.Shadows.shadowOf

class UserLoginTestUtil {
    companion object {
        fun setupUserAndMockLogin(user: User) {
            val activity = Robolectric.buildActivity(Activity::class.java).create().get()
            user.save(activity)
            Db.setUser(user)

            val accountType = activity.resources.getString(R.string.expedia_account_type_identifier)
            val manager = AccountManager.get(activity)
            val account = Account("test", accountType)
            val shadowAccountManager = shadowOf(manager)
            shadowAccountManager.addAccount(account)

            User.signIn(activity, null)
        }
    }
}