package com.expedia.bookings.data.user

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.activity.RestrictedProfileActivity
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.services.PersistentCookieManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.CookiesUtils
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.model.UserLoginStateChangedModel
import com.mobiata.android.util.SettingUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class UserStateManagerTests {
    private val expediaUrl = HttpUrl.Builder().scheme("https").host("www.expedia.com").build()

    lateinit private var notificationManager: NotificationManager
    lateinit private var userStateManager: UserStateManager

    val context = RuntimeEnvironment.application


    @Before
    fun setup() {
        SettingUtils.save(context, CookiesUtils.FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, true)

        notificationManager = NotificationManager(context)

        val userSource = UserSource(context, TestFileCipher("whatever"))

        userStateManager = UserStateManager(context, UserLoginStateChangedModel(), notificationManager, AccountManager.get(context), userSource)
    }

    @Test
    fun testRestrictedProfileStartsRestrictedProfileActivity() {
        val testActivity = Robolectric.buildActivity(Activity::class.java).create().get()

        userStateManager.signIn(testActivity, null, TestRestrictedProfileSource())

        val intent = Shadows.shadowOf(testActivity).nextStartedActivity

        assertNotNull(intent)
        assertEquals(RestrictedProfileActivity::class.java.name, intent.component.className)
    }

    @Test
    fun testSignInAddsAccountToManager() {
        val testActivity = Robolectric.buildActivity(Activity::class.java).create().get()
        val testLoginProvider = Mockito.mock(AccountLoginProvider::class.java)

        userStateManager.signIn(testActivity, null, null, testLoginProvider)

        val accountType = testActivity.getString(R.string.expedia_account_type_identifier)
        val tokenType = testActivity.getString(R.string.expedia_account_token_type_tuid_identifier)

        Mockito.verify(testLoginProvider).addAccount(accountType, tokenType, null, null, testActivity, null, null)
    }

    @Test
    fun testSignInWithActiveAccountRequestsAuthToken() {
        val testActivity = Robolectric.buildActivity(Activity::class.java).create().get()
        val testLoginProvider = Mockito.mock(AccountLoginProvider::class.java)

        val accountType = testActivity.getString(R.string.expedia_account_type_identifier)
        val testAccount = Account("Test", "Test")

        Mockito.`when`(testLoginProvider.getAccountsByType(accountType)).thenReturn(arrayOf(testAccount))

        userStateManager.signIn(testActivity, null, null, testLoginProvider)

        Mockito.verify(testLoginProvider).getAuthToken(testAccount, accountType, null, testActivity, null, null)
    }

    @Test
    fun notSignedInUserHasNoLoyaltyTier() {
        val expectedTier = LoyaltyMembershipTier.NONE

        assertFalse(userStateManager.isUserAuthenticated())
        assertEquals(expectedTier, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun userWithNoLoyaltyInfoHasNoLoyaltyTier() {
        val expectedTier = LoyaltyMembershipTier.NONE

        givenSignedInAsUser(getNonRewardsMember())

        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(expectedTier, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun userWithLoyaltyInfoHasCorrectLoyaltyTier() {
        givenSignedInAsUser(getBaseTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.BASE, userStateManager.getCurrentUserLoyaltyTier())

        givenSignedInAsUser(getMiddleTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.MIDDLE, userStateManager.getCurrentUserLoyaltyTier())

        givenSignedInAsUser(getTopTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.TOP, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun testUserIsLoadedIfNotInMemoryWhenIsUserAuthenticatedIsCalled() {
        class TestUserSource: UserSource(RuntimeEnvironment.application) {
            var didCallLoadUser = false
                private set
            override var user: User? = null
                get() = null

            override fun loadUser() {
                didCallLoadUser = true
            }
        }

        val testManager = Mockito.mock(AccountManager::class.java)
        val accountType = RuntimeEnvironment.application.getString(R.string.expedia_account_type_identifier)
        val tokenType = RuntimeEnvironment.application.getString(R.string.expedia_account_token_type_tuid_identifier)

        val testAccount = Account("Test", "Test")

        Mockito.`when`(testManager.getAccountsByType(accountType)).thenReturn(arrayOf(testAccount))
        Mockito.`when`(testManager.peekAuthToken(testAccount, tokenType)).thenReturn("AuthToken")

        val testUserSource = TestUserSource()

        val testUserStateManager = UserStateManager(
                RuntimeEnvironment.application,
                UserLoginStateChangedModel(),
                notificationManager,
                testManager,
                testUserSource)

        givenSignedInAsUser(getBaseTierRewardsMember())
        testUserStateManager.isUserAuthenticated()

        assertTrue(testUserSource.didCallLoadUser)
    }

    @Test
    fun testUserStateSanityCallsForceAccountRefreshWhenUserIsNotLoggedInOnDisk() {
        val testRefresher = TestUserAccountRefresher()

        userStateManager.addUserToAccountManager(getBaseTierRewardsMember())
        userStateManager.ensureUserStateSanity(TestListener(), testRefresher)

        assertTrue(testRefresher.didCallForceAccountRefresh)
    }

    @Test
    fun testUserStateSanityCallsOnUserAccountRefreshedWhenUserLoggedIn() {
        val testListener = TestListener()

        givenSignedInAsUser(getBaseTierRewardsMember())
        userStateManager.ensureUserStateSanity(testListener)

        assertTrue(testListener.onUserAccountRefreshCalled)
    }

    @Test
    fun testUserDeletedAtSystemLevelSignedOutWhenLoggedInAccountChanges() {
        givenSignedInAsDiskOnlyUser(getBaseTierRewardsMember())
        assertTrue(userStateManager.isUserLoggedInOnDisk())

        userStateManager.onLoginAccountsChanged()

        assertFalse(userStateManager.isUserLoggedInOnDisk())
    }

    @Test
    fun testAddUserToAccountManagerWithNullUserDoesNothing() {
        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(null)

        assertTrue(manager.accounts.isEmpty())
    }

    @Test
    fun testAddUserToAccountManagerWithInvalidUserDoesNothing() {
        val user = UserLoginTestUtil.mockUser()

        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(user)

        assertTrue(manager.accounts.isEmpty())
    }

    @Test
    fun testAddUserToAccountManagerWithValidUserIsAdded() {
        val user = UserLoginTestUtil.mockUser()
        user.primaryTraveler.email = "test@expedia.com"

        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(user)

        val storedUserEmail = manager.accounts.first().name

        assertEquals(user.primaryTraveler.email, storedUserEmail)
    }

    @Test
    fun testAddUserToAccountManagerSkipsAlreadyKnownUser() {
        val user = UserLoginTestUtil.mockUser()
        user.primaryTraveler.email = "test@expedia.com"

        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(user)

        var storedUserEmail = manager.accounts.first().name

        assertEquals(user.primaryTraveler.email, storedUserEmail)

        userStateManager.addUserToAccountManager(user)

        assertTrue(manager.accounts.size == 1)

        storedUserEmail = manager.accounts.first().name

        assertEquals(user.primaryTraveler.email, storedUserEmail)
    }

    @Test
    fun testAddUserToAccountManagerReplacesKnownUserWithUserToBeAdded() {
        val firstUser = UserLoginTestUtil.mockUser()
        firstUser.primaryTraveler.email = "test@expedia.com"

        val secondUser = UserLoginTestUtil.mockUser()
        secondUser.primaryTraveler.email = "anotherTest@expedia.com"

        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(firstUser)

        var storedUserEmail = manager.accounts.first().name

        assertEquals(firstUser.primaryTraveler.email, storedUserEmail)

        userStateManager.addUserToAccountManager(secondUser)

        assertTrue(manager.accounts.size == 1)

        storedUserEmail = manager.accounts.first().name

        assertEquals(secondUser.primaryTraveler.email, storedUserEmail)
    }

    @Test
    fun testRemoveUserFromAccountManagerRemovesAccount() {
        val user = UserLoginTestUtil.mockUser()
        user.primaryTraveler.email = "test@expedia.com"

        val manager = AccountManager.get(RuntimeEnvironment.application)

        userStateManager.addUserToAccountManager(user)

        val storedUserEmail = manager.accounts.first().name

        assertEquals(user.primaryTraveler.email, storedUserEmail)

        userStateManager.removeUserFromAccountManager(user)

        assertTrue(manager.accounts.isEmpty())
    }

//    TODO: Re-enable tests when we have the functionality to restart application class in Roboelectric.
//    @Test
//    fun testSignOutPreservingCookiesPreservesCookies() {
//        val cookieManager = populateAndGetCookieManager()
//
//        var cookies = cookieManager.cookieStore[expediaUrl.host()]
//
//        assertTrue(cookies?.values?.size == 3)
//
//        userStateManager.signOutPreservingCookies()
//
//        cookies = cookieManager.cookieStore[expediaUrl.host()]
//
//        assertTrue(cookies?.values?.size == 3)
//    }
//
//    @Test
//    fun testSignOutClearsCookies() {
//        val cookieManager = populateAndGetCookieManager()
//
//        var cookies = cookieManager.cookieStore[expediaUrl.host()]
//
//        assertTrue(cookies?.values?.size == 3)
//
//        userStateManager.signOut()
//
//        cookies = cookieManager.cookieStore[expediaUrl.host()]
//
//        assertTrue(cookies?.values?.size == 0)
//    }

    @Test
    fun testSignOutClearsWorkingBillingInfo() {
        val billingInfo = BillingInfo()
        billingInfo.email = "test@expedia.com"

        Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(billingInfo)

        assertNotNull(Db.getWorkingBillingInfoManager().workingBillingInfo.email)

        userStateManager.signOut()

        assertNull(Db.getWorkingBillingInfoManager().workingBillingInfo.email)
    }

    @Test
    fun testSignOutClearsWorkingTravelerInfo() {
        val traveler = Traveler()
        traveler.email = "test@expedia.com"

        Db.getWorkingTravelerManager().setWorkingTravelerAndBase(traveler)

        assertNotNull(Db.getWorkingTravelerManager().workingTraveler.email)

        userStateManager.signOut()

        assertNull(Db.getWorkingTravelerManager().workingTraveler.email)
    }

    @Test
    fun testSignOutResetsBillingInfo() {
        val billingInfo = BillingInfo()
        billingInfo.email = "test@expedia.com"

        Db.sharedInstance.setBillingInfo(billingInfo)

        assertNotNull(Db.getBillingInfo().email)

        userStateManager.signOut()

        assertNull(Db.getBillingInfo().email)
    }

    @Test
    fun testSignOutResetsTravelers() {
        val traveler = Traveler()
        traveler.email = "test@expedia.com"

        Db.sharedInstance.setTravelers(listOf(traveler))

        assertNotNull(Db.sharedInstance.travelers.first().email)

        userStateManager.signOut()

        assertNull(Db.sharedInstance.travelers.first().email)
    }

    @Test
    fun testSignOutClearsAirAttachFromTripBucket() {
        val jsonObject = JSONObject()
        jsonObject.put("airAttachQualified", true)

        val expirationTime = JSONObject()
        expirationTime.put("epochSeconds", (System.currentTimeMillis() / 1000L) + 1000L)
        expirationTime.put("timeZoneOffsetSeconds", -28800)

        jsonObject.put("offerExpiresTime", expirationTime)

        val airAttach = AirAttach(jsonObject)

        Db.getTripBucket().airAttach = airAttach

        assertNotNull(Db.getTripBucket().airAttach)

        userStateManager.signOut()

        assertNull(Db.getTripBucket().airAttach)
    }

    @Test
    fun testIsUserLoggedInOnDiskReturnsFalseForNoSavedUserData() {
        val userData = RuntimeEnvironment.application.getFileStreamPath("user.dat")

        assertFalse(userData.exists())
        assertFalse(userStateManager.isUserLoggedInOnDisk())
    }

    @Test
    fun testIsUserLoggedInOnDiskReturnsTrueForSavedUserData() {
        givenSignedInAsDiskOnlyUser(getBaseTierRewardsMember())

        val userData = RuntimeEnvironment.application.getFileStreamPath("user.dat")

        assertTrue(userData.exists())
        assertTrue(userStateManager.isUserLoggedInOnDisk())
    }

    @Test
    fun testIsUserLoggedInToAccountManagerReturnsFalseWhenNoUsersPresent() {
        assertFalse(userStateManager.isUserLoggedInToAccountManager())
    }

    @Test
    fun testIsUserLoggedInToAccountManagerReturnsFalseWhenAuthTokenIsNull() {
        val testManager = Mockito.mock(AccountManager::class.java)
        val accountType = RuntimeEnvironment.application.getString(R.string.expedia_account_type_identifier)
        val tokenType = RuntimeEnvironment.application.getString(R.string.expedia_account_token_type_tuid_identifier)

        val testAccount = Account("Test", "Test")

        Mockito.`when`(testManager.getAccountsByType(accountType)).thenReturn(arrayOf(testAccount))
        Mockito.`when`(testManager.peekAuthToken(testAccount, tokenType)).thenReturn(null)

        assertFalse(userStateManager.isUserLoggedInToAccountManager())
    }

    @Test
    fun testIsUserLoggedInToAccountManagerReturnsTrueWhenUserIsPresent() {
        val testManager = Mockito.mock(AccountManager::class.java)
        val accountType = RuntimeEnvironment.application.getString(R.string.expedia_account_type_identifier)
        val tokenType = RuntimeEnvironment.application.getString(R.string.expedia_account_token_type_tuid_identifier)

        val testAccount = Account("Test", "Test")

        Mockito.`when`(testManager.getAccountsByType(accountType)).thenReturn(arrayOf(testAccount))
        Mockito.`when`(testManager.peekAuthToken(testAccount, tokenType)).thenReturn("AuthToken")

        val testUserStateManager = UserStateManager(
                RuntimeEnvironment.application,
                UserLoginStateChangedModel(),
                notificationManager,
                testManager)

        assertTrue(testUserStateManager.isUserLoggedInToAccountManager())
    }

    @Test
    fun testIsUserAuthenticatedLogsExceptionWhenThrown() {
        val testManager = Mockito.mock(AccountManager::class.java)
        val accountType = RuntimeEnvironment.application.getString(R.string.expedia_account_type_identifier)
        val tokenType = RuntimeEnvironment.application.getString(R.string.expedia_account_token_type_tuid_identifier)

        val testAccount = Account("Test", "Test")

        UserLoginTestUtil.createEmptyUserDataFile()

        Mockito.`when`(testManager.getAccountsByType(accountType)).thenReturn(arrayOf(testAccount))
        Mockito.`when`(testManager.peekAuthToken(testAccount, tokenType)).thenReturn("AuthToken")

        val loggingProvider = TestExceptionLoggingProvider()

        val testUserStateManager = UserStateManager(
                RuntimeEnvironment.application,
                UserLoginStateChangedModel(),
                notificationManager,
                testManager,
                UserSource(RuntimeEnvironment.application),
                loggingProvider)

        assertFalse(testUserStateManager.isUserAuthenticated())
        assertTrue(loggingProvider.didLogException)
    }

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user, userStateManager)
    }

    private fun givenSignedInAsDiskOnlyUser(user: User) {
        userStateManager.userSource.user = user
    }

    private fun getNonRewardsMember(): User {
        val user = User()
        val traveler = Traveler()

        traveler.firstName = "No"
        traveler.middleName = "Rewards"
        traveler.lastName = "ForMe"
        traveler.email = "norewards@mobiata.com"
        user.primaryTraveler = traveler

        return user
    }

    private fun getBaseTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Base"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "basetier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.BASE
        loyaltyInfo.loyaltyPointsAvailable = 1802.0
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getMiddleTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Middle"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "middletier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.MIDDLE
        loyaltyInfo.loyaltyPointsAvailable = 22996.0
        loyaltyInfo.loyaltyPointsPending = 965.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("3285.14", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$3,285.14")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getTopTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Top"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "toptier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 54206.0
        loyaltyInfo.loyaltyPointsPending = 5601.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("7743.41", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$7,743.41")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun populateAndGetCookieManager(): PersistentCookieManager {
        val services =  ExpediaServices(RuntimeEnvironment.application)
        val cookieManager = services.mCookieManager as PersistentCookieManager

        val cookiePairs = hashMapOf(Pair("user", Cookie.parse(expediaUrl, "user=user")),
                Pair("minfo", Cookie.parse(expediaUrl, "minfo=minfo")),
                Pair("accttype", Cookie.parse(expediaUrl, "accttype=accttype")))

        cookieManager.cookieStore.put(expediaUrl.host(), cookiePairs)

        return cookieManager
    }

    private class TestRestrictedProfileSource: RestrictedProfileSource(Activity()) {
        override fun isRestrictedProfile(): Boolean = true
    }

    private class TestListener: UserAccountRefresher.IUserAccountRefreshListener {
        var onUserAccountRefreshCalled = false
            private set

        override fun onUserAccountRefreshed() {
            onUserAccountRefreshCalled = true
        }
    }

    private class TestUserAccountRefresher: UserAccountRefresher(RuntimeEnvironment.application, LineOfBusiness.NONE, TestListener()) {
        var didCallForceAccountRefresh = false
            private set

        override fun forceAccountRefresh() {
            didCallForceAccountRefresh = true
        }
    }

    private class TestExceptionLoggingProvider: ExceptionLoggingProvider() {
        var didLogException = false
            private set

        override fun logException(throwable: Throwable) {
            didLogException = true
        }
    }
}
