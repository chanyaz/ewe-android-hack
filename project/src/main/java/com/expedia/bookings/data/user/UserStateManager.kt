package com.expedia.bookings.data.user

import android.content.Context
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
}