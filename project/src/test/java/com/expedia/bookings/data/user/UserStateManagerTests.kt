package com.expedia.bookings.data.user

import android.accounts.AccountManager
import android.content.Context
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
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
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.model.UserLoginStateChangedModel
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class UserStateManagerTests {

    val expediaUrl = HttpUrl.Builder().scheme("https").host("www.expedia.com").build()

    lateinit private var notificationManager: NotificationManager
    lateinit private var userStateManager: UserStateManager

    @Before
    fun setup() {
        val context: Context = RuntimeEnvironment.application
        notificationManager = NotificationManager(context)
        userStateManager = UserStateManager(context, UserLoginStateChangedModel(), notificationManager)
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
    fun testUserStateSanityCallsOnUserAccountRefreshedWhenUserLoggedIn() {
        var onUserAccountRefreshCalled = false

        class TestListener: UserAccountRefresher.IUserAccountRefreshListener {
            override fun onUserAccountRefreshed() {
                onUserAccountRefreshCalled = true
            }
        }

        givenSignedInAsUser(getBaseTierRewardsMember())
        userStateManager.ensureUserStateSanity(TestListener())

        assertTrue(onUserAccountRefreshCalled)
    }

    @Test
    fun testUserDeletedAtSystemLevelSignedOutWhenLoggedInAccountChanges() {
        givenSignedInAsDiskOnlyUser(getBaseTierRewardsMember())
        assertTrue(User.isLoggedInOnDisk(RuntimeEnvironment.application))

        userStateManager.onLoginAccountsChanged()

        assertFalse(User.isLoggedInOnDisk(RuntimeEnvironment.application))
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

    @Test
    fun testSignOutPreservingCookiesPreservesCookies() {
        val cookieManager = populateAndGetCookieManager()

        var cookies = cookieManager.cookieStore.get(expediaUrl.host())

        assertTrue(cookies?.values?.size == 3)

        userStateManager.signOutPreservingCookies()

        cookies = cookieManager.cookieStore.get(expediaUrl.host())

        assertTrue(cookies?.values?.size == 3)
    }

    @Test
    fun testSignOutClearsCookies() {
        val cookieManager = populateAndGetCookieManager()

        var cookies = cookieManager.cookieStore.get(expediaUrl.host())

        assertTrue(cookies?.values?.size == 3)

        userStateManager.signOut()

        cookies = cookieManager.cookieStore.get(expediaUrl.host())

        assertTrue(cookies?.values?.size == 0)
    }

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

        Db.setBillingInfo(billingInfo)

        assertNotNull(Db.getBillingInfo().email)

        userStateManager.signOut()

        assertNull(Db.getBillingInfo().email)
    }

    @Test
    fun testSignOutResetsTravelers() {
        val traveler = Traveler()
        traveler.email = "test@expedia.com"

        Db.setTravelers(listOf(traveler))

        assertNotNull(Db.getTravelers().first().email)

        userStateManager.signOut()

        assertNull(Db.getTravelers().first().email)
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

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenSignedInAsDiskOnlyUser(user: User) {
        UserLoginTestUtil.setupUserAndMockDiskOnlyLogin(user)
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

}