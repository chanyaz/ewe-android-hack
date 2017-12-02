package com.expedia.bookings.activity

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.UserAccountRefresher
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class ExpediaBookingAppTests {
    @Test
    fun testLoginUserIfApplicableCallsForceAccountRefresh() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val application = RuntimeEnvironment.application as ExpediaBookingApp
        val testRefresher = TestUserAccountRefresher()
        val userStateManager = UserLoginTestUtil.getUserStateManager()

        application.loginUserIfApplicable(testRefresher, userStateManager)

        assertTrue(testRefresher.didForceAccountRefresh)
    }

    class TestUserAccountRefresher: UserAccountRefresher(RuntimeEnvironment.application, LineOfBusiness.NONE, null) {
        var didForceAccountRefresh = false
            private set

        override fun forceAccountRefresh() {
            didForceAccountRefresh = true
        }
    }
}
