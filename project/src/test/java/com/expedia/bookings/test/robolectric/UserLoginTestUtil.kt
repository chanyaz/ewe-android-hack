package com.expedia.bookings.test.robolectric

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.utils.Ui
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

class UserLoginTestUtil {
    companion object {
        @JvmStatic
        fun setupUserAndMockLogin(user: User) {
            setupUserAndMockLogin(user, getContext())
        }

        @JvmStatic
        fun setupUserAndMockLogin(user: User, activity: Activity) {

            setupUserAndMockDiskOnlyLogin(user, activity)

            Db.setUser(user)

            val accountType = activity.resources.getString(R.string.expedia_account_type_identifier)
            val manager = AccountManager.get(activity)
            val account = Account("test", accountType)
            val shadowAccountManager = shadowOf(manager)
            shadowAccountManager.addAccount(account)

            Ui.getApplication(activity).appComponent().userStateManager().signIn(activity)
        }

        @JvmStatic
        fun setupUserAndMockDiskOnlyLogin(user: User, activity: Activity = getContext()) {
            user.save(activity)
        }

        @JvmStatic
        fun mockUser(membershipTier: LoyaltyMembershipTier): User {
            val user = User()
            val traveler = Traveler()
            user.primaryTraveler = traveler
            val loyaltyInfo = UserLoyaltyMembershipInformation()
            loyaltyInfo.loyaltyMembershipTier = membershipTier
            user.loyaltyMembershipInformation = loyaltyInfo
            return user
        }

        @JvmStatic
        fun mockUser(): User {
            return this.mockUser(LoyaltyMembershipTier.TOP)
        }

        private fun getContext(): Activity {
            return Robolectric.buildActivity(Activity::class.java).create().get()
        }
    }
}