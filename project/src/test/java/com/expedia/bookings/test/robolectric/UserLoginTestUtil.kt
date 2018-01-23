package com.expedia.bookings.test.robolectric

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.TestFileCipher
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.user.UserSource
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.mobiata.android.util.IoUtils
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

class UserLoginTestUtil {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun setupUserAndMockLogin(user: User, userStateManager: UserStateManager = getDefaultUserStateManager(getContext())) {
            setupUserAndMockLogin(user, getContext(), userStateManager)
        }

        @JvmStatic
        fun getUserStateManager(context: Context = RuntimeEnvironment.application,
                                userSource: UserSource = UserSource(context, TestFileCipher("whatever"))): UserStateManager =
                UserStateManager(context, UserLoginStateChangedModel(), NotificationManager(context), AccountManager.get(context), userSource)

        @JvmStatic
        private fun getDefaultUserStateManager(context: Context): UserStateManager = Ui.getApplication(context).appComponent().userStateManager()

        @JvmStatic
        fun setupUserAndMockLogin(user: User, activity: Activity = getContext(), userStateManager: UserStateManager = getDefaultUserStateManager(activity)) {
            userStateManager.userSource.user = user

            val accountType = activity.resources.getString(R.string.expedia_account_type_identifier)
            val manager = AccountManager.get(activity)
            val account = Account("test", accountType)
            val shadowAccountManager = shadowOf(manager)
            shadowAccountManager.addAccount(account)

            userStateManager.signIn(activity)
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
        fun mockUser(): User = this.mockUser(LoyaltyMembershipTier.TOP)

        @JvmStatic
        fun createEmptyUserDataFile() {
            IoUtils.writeStringToFile("user.dat", "", RuntimeEnvironment.application)
        }

        private fun getContext(): Activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }
}
