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
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.utils.CarnivalUtils
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.model.UserLoginStateChangedModel

class UserStateManager @JvmOverloads constructor(private val context: Context,
                       private val userLoginStateChangedModel: UserLoginStateChangedModel,
                       private val notificationManager: NotificationManager,
                       private val accountManager: AccountManager = AccountManager.get(context),
                       val userSource: UserSource = UserSource(context),
                       private val loggingProvider: ExceptionLoggingProvider = ExceptionLoggingProvider()) {
    private val SAVED_INFO_FILENAME = "user.dat"

    private val accountType: String by lazy {
        context.getString(R.string.expedia_account_type_identifier)
    }
    private val tokenType: String by lazy {
        context.getString(R.string.expedia_account_token_type_tuid_identifier)
    }
    private val contentAuthority: String by lazy {
        context.getString(R.string.authority_account_sync)
    }

    @JvmOverloads
    fun signIn(activity: Activity, options: Bundle? = null, restrictedProfileSource: RestrictedProfileSource? = null, loginProvider: AccountLoginProvider? = null) {
        performSignIn(activity, options, restrictedProfileSource ?: RestrictedProfileSource(activity), loginProvider ?: AccountLoginProvider(accountManager))
    }

    private fun performSignIn(activity: Activity, options: Bundle?, restrictedProfileSource: RestrictedProfileSource, loginProvider: AccountLoginProvider) {
        if (restrictedProfileSource.isRestrictedProfile()) {
            val restrictedProfileIntent = RestrictedProfileActivity.createIntent(context)
            activity.startActivity(restrictedProfileIntent)
        }
        else {
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
        CarnivalUtils.getInstance().clearUserInfo()
    }

    fun isUserAuthenticated(): Boolean {
        if (isUserLoggedInOnDisk() && isUserLoggedInToAccountManager()) {
            if (userSource.user == null) {
                try {
                    userSource.loadUser()
                }
                catch (e: Exception) {
                    loggingProvider.logException(e)
                    return false
                }
            }

            return true
        }

        return false
    }

    fun isUserLoggedInOnDisk(): Boolean {
        val file = context.getFileStreamPath(SAVED_INFO_FILENAME)
        return file != null && file.exists()
    }

    fun isUserLoggedInToAccountManager(): Boolean {
        val activeAccount = accountManager.getAccountsByType(accountType).firstOrNull()

        return activeAccount != null && !accountManager.peekAuthToken(activeAccount, tokenType).isNullOrEmpty()
    }

    fun onLoginAccountsChanged() {
        if (isUserLoggedInOnDisk() && !isUserLoggedInToAccountManager()) {
            // User deleted account from System-level accounts screen, so reflect that change internally
            signOut()

            if (!ExpediaBookingApp.isAutomation()) {
                ItineraryManager.getInstance().startSync(true)
            }
        }
    }

    @JvmOverloads
    fun ensureUserStateSanity(listener: UserAccountRefresher.IUserAccountRefreshListener,
                              refresher: UserAccountRefresher? = null) {
        if (isUserLoggedInToAccountManager() && !isUserLoggedInOnDisk()) {
            (refresher ?: UserAccountRefresher(context, LineOfBusiness.NONE, listener)).forceAccountRefresh()
        } else {
            listener.onUserAccountRefreshed()
        }
    }

    fun getCurrentUserLoyaltyTier(): LoyaltyMembershipTier {
        if (isUserAuthenticated()) {
            return userSource.user?.loyaltyMembershipInformation?.loyaltyMembershipTier ?: LoyaltyMembershipTier.NONE
        }

        return LoyaltyMembershipTier.NONE
    }

    fun addUserToAccountManager(user: User?) {
        if (user != null && user.primaryTraveler.email != null && user.primaryTraveler.email.isNotEmpty()) {
            var accountExists = false

            val accounts = accountManager.getAccountsByType(accountType)

            if (accounts?.isNotEmpty() == true) {
                accounts.forEach {
                    if (it.name == user.primaryTraveler.email) accountExists = true
                    else accountManager.removeAccount(it, null, null)
                }
            }

            if (!accountExists) {
                val account = Account(user.primaryTraveler.email, accountType)
                accountManager.addAccountExplicitly(account, user.tuidString, null)
                accountManager.setAuthToken(account, tokenType, user.tuidString)

                if (!ExpediaBookingApp.isAutomation()) {
                    ContentResolver.setSyncAutomatically(account, contentAuthority, false)
                }
            }
        }
    }

    fun removeUserFromAccountManager(user: User?) {
        val accounts = accountManager.getAccountsByType(accountType)

        if (accounts?.isNotEmpty() == true) {
            val account = accounts.first()
            ContentResolver.setIsSyncable(account, contentAuthority, 0)
            accountManager.removeAccount(account, null, null)
        }

        if (user != null) {
            accountManager.invalidateAuthToken(accountType, user.tuidString)
        }
    }

    private fun performSignOutCriticalActions(clearCookies: Boolean) {
        val logger = TimingLogger("ExpediaBookings", "User.performCriticalSignOutActions")

        deleteSavedUserInfo()
        logger.addSplit("delete()")

        removeUserFromAccountManager(userSource.user)
        logger.addSplit("removeUserFromAccountManager()")

        userSource.user = null
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