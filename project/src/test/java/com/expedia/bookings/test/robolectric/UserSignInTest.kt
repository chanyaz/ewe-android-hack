package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.utils.MockModeShim
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class UserSignInTest {

    var expediaServices : ExpediaServices by Delegates.notNull()
    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun before() {
        MockModeShim.initMockWebServer(getContext())
        SettingUtils.save(getContext(), getContext().getString(R.string.preference_which_api_to_use_key), "Mock Mode")
        expediaServices = ExpediaServices(getContext())
    }

    @Test
    fun testSignInWithNoStoredCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "nostoredcards@mobiata.com")
        val user = signInResponse.user
        assertEquals(0, user.storedCreditCards.size)
        assertEquals(0, user.storedPointsCards.size)
        assertEquals("", user.expediaRewardsMembershipId)
    }

    @Test
    fun testSignInWithOnlyStoredCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "singlecard@mobiata.com")
        val user = signInResponse.user
        assertEquals(1, user.storedCreditCards.size)
        assertNotNull(user.storedCreditCards[0].id)
        assertEquals(0, user.storedPointsCards.size)
        assertEquals("", user.expediaRewardsMembershipId)
    }

    @Test
    fun testSignInWithOnlyStoredPointsCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "singlePointscard@mobiata.com")
        val user = signInResponse.user
        assertEquals(0, user.storedCreditCards.size)
        assertEquals(1, user.storedPointsCards.size)
        assertNotNull(user.storedPointsCards[0].paymentsInstrumentId)
        assertNotNull(user.storedPointsCards[0].paymentType)
        assertEquals("123456", user.expediaRewardsMembershipId)
    }

    @Test
    fun testSignInWithBothStoredCreditCardAndStoredPointsCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "singleCreditCardAndPointsCard@mobiata.com")
        val user = signInResponse.user
        assertEquals(1, user.storedCreditCards.size)
        assertNotNull(user.storedCreditCards[0].id)
        assertEquals(1, user.storedPointsCards.size)
        assertNotNull(user.storedPointsCards[0].paymentsInstrumentId)
        assertNotNull(user.storedPointsCards[0].paymentType)
        assertEquals("123456", user.expediaRewardsMembershipId)
    }

    @Test
    fun testGetStoredPointsCardWithSingleStoredPointsCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "singlePointscard@mobiata.com")
        assertNotNull(signInResponse.user.getStoredPointsCard(PaymentType.POINTS_EXPEDIA_REWARDS))
    }

    @Test
    fun testGetStoredPointsCardWithNoPointsCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "singlecard@mobiata.com")
        assertNull(signInResponse.user.getStoredPointsCard(PaymentType.POINTS_EXPEDIA_REWARDS))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testGetStoredPointsThrowsErrorWithCreditCard() {
        val signInResponse = expediaServices.signInWithEmailForAutomationTests(ExpediaServices.F_HOTELS, "nostoredcards@mobiata.com")
        signInResponse.user.getStoredPointsCard(PaymentType.CARD_AMERICAN_EXPRESS)
    }
}