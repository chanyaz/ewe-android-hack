package com.expedia.bookings.tracking

import android.content.Context
import com.adobe.adms.measurement.ADMS_Measurement
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.DebugInfoUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class OmnitureTrackingTest {

    private val USER_EMAIL = "testuser@expedia.com"
    private val USER_EMAIL_HASH = "1941c6bff303b2fb1af6801a7eb809e657bc611e8e2d76c44961b90aec193f5a"

    private lateinit var context: Context;
    private lateinit var adms: ADMS_Measurement;

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        adms = ADMS_Measurement.sharedInstance(context)
    }

    @Test
    fun guidSentInProp23() {
        OmnitureTracking.trackAccountPageLoad()

        val expectedGuid = DebugInfoUtils.getMC1CookieStr(context).replace("GUID=", "")
        assertEquals(expectedGuid, adms.getProp(23))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun emailHashedWithSHA256() {
        givenUserIsSignedIn()

        OmnitureTracking.trackAccountPageLoad()

        assertEquals(USER_EMAIL_HASH, adms.getProp(11))
    }

    private fun givenUserIsSignedIn() {
        val user = UserLoginTestUtil.mockUser()
        user.primaryTraveler.email = USER_EMAIL
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

}