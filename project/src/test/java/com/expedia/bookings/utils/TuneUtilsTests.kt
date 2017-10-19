package com.expedia.bookings.utils

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.user.User
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.tune.TuneEvent
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

@RunWith(RobolectricRunner::class)
class TuneUtilsTests {
    private lateinit var provider: TestTuneTrackingProviderImpl

    @After
    fun tearDown() {
        TuneUtils.init(null)
    }

    @Test
    fun testInitUpdatesPOSAndTracksLaunchEvent() {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.BASE))

        assertTrue(provider.posData.isEmpty())
        assertNull(provider.trackedEvent)

        TuneUtils.init(provider)

        assertFalse(provider.posData.isEmpty())
        assertNotNull(provider.trackedEvent)
        assertEquals("Custom_Open", provider.trackedEvent?.eventName)
        assertEquals("0", provider.trackedEvent?.attribute1)
        assertEquals("0", provider.trackedEvent?.attribute2)
        assertEquals(LoyaltyMembershipTier.BASE.toApiValue(), provider.trackedEvent?.attribute3)
        assertNull(provider.trackedEvent?.attribute4)
    }

    @Test
    fun testSetFacebookReferralUrlEquality() {
        val expectedUrlString = "http://expedia.com"

        provider = TestTuneTrackingProviderImpl()
        TuneUtils.init(provider)

        assertTrue(provider.facebookReferralUrlString.isEmpty())

        TuneUtils.setFacebookReferralUrl(expectedUrlString)

        assertEquals(expectedUrlString, provider.facebookReferralUrlString)
    }

    private class TestTuneTrackingProviderImpl(private val user: User? = UserLoginTestUtil.mockUser(),
                                               private val isLoggedIn: Boolean = false): TuneTrackingProvider {
        var trackedEvent: TuneEvent? = null
            private set
        override val authenticatedUser: User?
            get() = user
        override val tuid: String
            get() = authenticatedUser?.tuidString ?: ""
        override val membershipTier: String?
            get() = authenticatedUser?.loyaltyMembershipInformation?.loyaltyMembershipTier?.toApiValue()
        override val isUserLoggedInValue: String
            get() = if (isLoggedIn) "1" else "0"
        override var posData: String = ""
        override var facebookReferralUrlString: String = ""

        override fun trackEvent(event: TuneEvent) {
            trackedEvent = event
        }

        override fun didFailDeeplink(error: String?) { TODO("not implemented") }
        override fun didReceiveDeeplink(deeplink: String?) { TODO("not implemented") }
    }
}
