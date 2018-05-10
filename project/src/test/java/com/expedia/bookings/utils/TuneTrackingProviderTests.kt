package com.expedia.bookings.utils

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = [(ShadowGCM::class), (ShadowUserManager::class), (ShadowAccountManagerEB::class)])
class TuneTrackingProviderTests {
    @Test
    fun testAuthenticatedUserReturnsUserWhenAuthenticated() {
        assertNotNull(givenTuneTrackingProviderWithLoggedInUser().authenticatedUser)
    }

    @Test
    fun testAuthenticatedUserReturnsNullWhenNotAuthenticated() {
        assertNull(givenTuneTrackingProvider().authenticatedUser)
    }

    @Test
    fun testTuidReturnedForAuthenticatedUser() {
        assertEquals("0", givenTuneTrackingProviderWithLoggedInUser().tuid)
    }

    @Test
    fun testTuidReturnsNullWhenUserNotAuthenticated() {
        assertEquals("", givenTuneTrackingProvider().tuid)
    }

    @Test
    fun testMembershipTierReturnsNullWhenUserNotAuthenticated() {
        assertNull(givenTuneTrackingProvider().membershipTier)
    }

    @Test
    fun testMembershipTierReturnsValueWhenUserAuthenticated() {
        assertEquals(LoyaltyMembershipTier.TOP.toApiValue(), givenTuneTrackingProviderWithLoggedInUser().membershipTier)
    }

    @Test
    fun testAbacusGUIDAssignsToTuneFacebookUserId() {
        val guid = UUID.randomUUID().toString()
        Db.sharedInstance.abacusGuid = guid

        val provider = givenTuneTrackingProvider()

        assertEquals(guid, provider.duaid)
        assertEquals(Db.sharedInstance.abacusGuid, provider.duaid)
    }

    @Test
    fun testNullAbacusGUIDAssignsEmptyStringToTuneFacebookUserId() {
        Db.sharedInstance.abacusGuid = null

        val provider = givenTuneTrackingProvider()

        assertNull(Db.sharedInstance.abacusGuid)
        assertEquals("", provider.duaid)
    }

    @Test
    fun testIsUserLoggedInValueWhenUserAuthenticated() {
        assertEquals("1", givenTuneTrackingProviderWithLoggedInUser().isUserLoggedInValue)
    }

    @Test
    fun testIsUserLoggedInValueWhenUserNotAuthenticated() {
        assertEquals("0", givenTuneTrackingProvider().isUserLoggedInValue)
    }

    @Test
    fun testPOSDataToTuneTwitterUserIdEquality() {
        val testTune = MockTune()
        val provider = givenTuneTrackingProvider(false, RuntimeEnvironment.application, testTune)

        assertTrue(testTune.twitterUserId.isEmpty())

        provider.posData = "Test"

        assertEquals("Test", provider.posData)
        assertEquals("Test", testTune.twitterUserId)
    }

    @Test
    fun testFacebookReferralUrlStringEquality() {
        val testTune = MockTune()
        val provider = givenTuneTrackingProvider(false, RuntimeEnvironment.application, testTune)

        assertTrue(testTune.referralUrl.isEmpty())

        provider.facebookReferralUrlString = "Test"

        assertEquals("Test", provider.facebookReferralUrlString)
        assertEquals("Test", testTune.referralUrl)
    }

    @Test
    fun testTuneDeepLinkListenerDidReceiveDeepLinkHandler() {
        val testApplication = TestApplication()
        val testTune = MockTune()
        givenTuneTrackingProvider(false, testApplication, testTune)

        testTune.deepLinker.handleExpandedTuneLink("test")

        assertTrue(testApplication.contextWrapper.didStartActivity)
    }

    @Test
    fun testDidReceiveDeepLinkHandlerDoesNotStartActivityWhenDeepLinkIsNull() {
        val testApplication = TestApplication()
        val testTune = MockTune()
        givenTuneTrackingProvider(false, testApplication, testTune)

        testTune.deepLinker.handleExpandedTuneLink(null)

        assertFalse(testApplication.contextWrapper.didStartActivity)
    }

    @Test
    fun testWhenExistingUserAndOlderOrbitzVersionTuneExistingUserIsSetToTrue() {
        val sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("loginPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("anonId", "anonId").apply()
        val testApplication = TestApplication()
        val testTune = MockTune()
        givenTuneTrackingProvider(false, testApplication, testTune, true)

        assertTrue(testTune.existingUser)
    }

    private fun givenTuneTrackingProviderWithLoggedInUser(): TuneTrackingProvider = givenTuneTrackingProvider(true)

    private fun givenTuneTrackingProvider(withLoggedInUser: Boolean = false, application: Application = RuntimeEnvironment.application, tuneInstance: MockTune = MockTune(), shouldSetExistingUser: Boolean = false): TuneTrackingProvider {
        val userStateManager = UserLoginTestUtil.getUserStateManager()

        if (withLoggedInUser) {
            UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser(), userStateManager)
        }

        return TuneTrackingProviderImpl(tuneInstance, application, userStateManager, shouldSetExistingUser)
    }

    private class TestContextWrapper : ContextWrapper(RuntimeEnvironment.application) {
        var didStartActivity = false
            private set

        override fun startActivity(intent: Intent?) {
            didStartActivity = true
        }
    }

    private class TestApplication : Application() {
        var contextWrapper = TestContextWrapper()

        override fun getApplicationContext(): Context = contextWrapper
    }
}
