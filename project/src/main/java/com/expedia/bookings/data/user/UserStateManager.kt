package com.expedia.bookings.data.user

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.UserAccountRefresher

class UserStateManager(val context: Context) {

    fun isUserAuthenticated(): Boolean {
        return User.isLoggedIn(context)
    }

    fun onLoginAccountsChanged() {
        if (User.isLoggedInOnDisk(context) && !User.isLoggedInToAccountManager(context)) {
            // User deleted account from System-level accounts screen, so reflect that change internally
            User.signOut(context)

            if (!ExpediaBookingApp.isAutomation()) {
                ItineraryManager.getInstance().startSync(true)
            }
        }
    }

    fun ensureUserStateSanity(listener: UserAccountRefresher.IUserAccountRefreshListener) {
        if (User.isLoggedInToAccountManager(context) && !User.isLoggedInOnDisk(context)) {
            User.loadUser(context, listener)
        } else {
            listener.onUserAccountRefreshed()
        }
    }

    fun getCurrentUserLoyaltyTier(): LoyaltyMembershipTier {
        if (isUserAuthenticated()) {
            if (Db.getUser() == null) {
                Db.loadUser(context)
            }
            return Db.getUser()?.loyaltyMembershipInformation?.loyaltyMembershipTier ?: LoyaltyMembershipTier.NONE
        }

        return LoyaltyMembershipTier.NONE
    }

    fun addUserToAccountManager(user: User?) {
        if (user != null && user.primaryTraveler.email != null && user.primaryTraveler.email.isNotEmpty()) {
            val accountType = context.getString(R.string.expedia_account_type_identifier)
            val tokenType = context.getString(R.string.expedia_account_token_type_tuid_identifier)

            val manager = AccountManager.get(context)

            var accountExists = false

            val accounts = manager.getAccountsByType(accountType)

            if (accounts.isNotEmpty()) {
                accounts.forEach {
                    if (it.name == user.primaryTraveler.email) accountExists = true
                    else manager.removeAccount(it, null, null)
                }
            }

            if (!accountExists) {
                val account = Account(user.primaryTraveler.email, accountType)
                manager.addAccountExplicitly(account, user.tuidString, null)
                manager.setAuthToken(account, tokenType, user.tuidString)

                val contentAuthority = context.getString(R.string.authority_account_sync)

                if (!ExpediaBookingApp.isAutomation()) {
                    ContentResolver.setSyncAutomatically(account, contentAuthority, false)
                }
            }
        }
    }
}