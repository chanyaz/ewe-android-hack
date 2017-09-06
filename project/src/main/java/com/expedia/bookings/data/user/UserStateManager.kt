package com.expedia.bookings.data.user

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.util.TimingLogger
import com.expedia.account.AccountService
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.activity.RestrictedProfileActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.model.UserLoginStateChangedModel

class UserStateManager(private val context: Context, private val userLoginStateChangedModel: UserLoginStateChangedModel, private val notificationManager: NotificationManager) {
    private val SAVED_INFO_FILENAME = "user.dat"

    @JvmOverloads
    fun signIn(activity: Activity, options: Bundle? = null, restrictedProfileSource: RestrictedProfileSource? = null, loginProvider: AccountLoginProvider? = null) {
        performSignIn(activity, options, restrictedProfileSource ?: RestrictedProfileSource(activity), loginProvider ?: AccountLoginProvider(AccountManager.get(context)))
    }

    private fun performSignIn(activity: Activity, options: Bundle?, restrictedProfileSource: RestrictedProfileSource, loginProvider: AccountLoginProvider) {
        if (restrictedProfileSource.isRestrictedProfile()) {
            val restrictedProfileIntent = RestrictedProfileActivity.createIntent(context)
            activity.startActivity(restrictedProfileIntent)
        }
        else {
            val accountType = activity.getString(R.string.expedia_account_type_identifier)
            val tokenType = activity.getString(R.string.expedia_account_token_type_tuid_identifier)

            val activeAccount = loginProvider.getAccountsByType(accountType)?.firstOrNull()

            if (activeAccount != null) {
                loginProvider.getAuthToken(activeAccount, accountType, options, activity, null, null)
            } else {
                loginProvider.addAccount(accountType, tokenType, null, options, activity, null, null)
            }
        }
    }

    fun signOutPreservingCookies() {
        performSignOut(false)
    }

    fun signOut() {
        performSignOut(true)
    }

    private fun performSignOut(clearCookies: Boolean) {
        val logger = TimingLogger("ExpediaBookings", "User.signOut")

        performSignOutCriticalActions(clearCookies)
        logger.addSplit("performSignOutCriticalActions")

        performSignOutCleanupActions()
        logger.addSplit("performSignOutCleanupActions")

        logger.dumpToLog()

        userLoginStateChangedModel.userLoginStateChanged.onNext(false)
    }

    fun isUserAuthenticated(): Boolean = User.isLoggedIn(context)

    fun onLoginAccountsChanged() {
        if (User.isLoggedInOnDisk(context) && !User.isLoggedInToAccountManager(context)) {
            // User deleted account from System-level accounts screen, so reflect that change internally
            signOut()

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

            if (accounts?.isNotEmpty() == true) {
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

    fun removeUserFromAccountManager(user: User?) {
        val accountType = context.getString(R.string.expedia_account_type_identifier)
        val contentAuthority = context.getString(R.string.authority_account_sync)

        val manager = AccountManager.get(context)
        val accounts = manager.getAccountsByType(accountType)

        if (accounts?.isNotEmpty() == true) {
            val account = accounts.first()
            ContentResolver.setIsSyncable(account, contentAuthority, 0)
            manager.removeAccount(account, null, null)
        }

        if (user != null) {
            manager.invalidateAuthToken(accountType, user.tuidString)
        }
    }

    private fun performSignOutCriticalActions(clearCookies: Boolean) {
        val logger = TimingLogger("ExpediaBookings", "User.performCriticalSignOutActions")

        deleteSavedUserInfo()
        logger.addSplit("delete()")

        removeUserFromAccountManager(Db.getUser())
        logger.addSplit("removeUserFromAccountManager()")

        Db.setUser(null)
        logger.addSplit("Db.setUser(null)")

        if (clearCookies) {
            ExpediaServices.removeUserLoginCookies(context)
            logger.addSplit("ExpediaServices.removeUserLoginCookies(context)")
        }

        AccountService.facebookLogOut()
        logger.addSplit("Facebook Session Closed")

        logger.dumpToLog()
    }

    private fun performSignOutCleanupActions() {
        val logger = TimingLogger("ExpediaBookings", "User.performSignOutCleanupActions")

        if (!ExpediaBookingApp.isRobolectric()) {
            ItineraryManager.getInstance().clear()
            logger.addSplit("ItineraryManager.getInstance().clear();")

            notificationManager.deleteAll()
            logger.addSplit("notificationManager.deleteAll();")
        }

        Db.getWorkingBillingInfoManager()?.clearWorkingBillingInfo()
        Db.getWorkingTravelerManager()?.clearWorkingTraveler()

        val tripBucket = Db.getTripBucket()

        if (tripBucket != null && tripBucket.isUserAirAttachQualified) {
            tripBucket.clearAirAttach()
            Db.saveTripBucket(context)
        }

        Db.resetBillingInfo()
        Db.resetTravelers()

        logger.addSplit("User billing and traveler info deletion.")
        logger.dumpToLog()
    }

    private fun deleteSavedUserInfo(): Boolean {
        val file = context.getFileStreamPath(SAVED_INFO_FILENAME)
        return file.exists() && file.delete()
    }
}