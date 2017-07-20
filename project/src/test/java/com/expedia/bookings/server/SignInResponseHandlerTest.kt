package com.expedia.bookings.server

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.user.User
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class SignInResponseHandlerTest {
    private val GOLD_STATUS_V1 = "{\"tuid\":673093611,\"expUserId\":45313772,\"email\":\"goldstatus@mobiata.com\",\"firstName\":\"Gold\",\"middleName\":\"\",\"lastName\":\"Status\",\"success\":true,\"detailedStatus\":\"Success\",\"detailedStatusMsg\":\"Already logged in\",\"phoneNumbers\":[{\"number\":\"6200790\",\"areaCode\":\"614\",\"category\":\"PRIMARY\",\"countryCode\":\"1\",\"extensionNumber\":\"\"}],\"storedCreditCards\":[{\"description\":\"Visa 1111\",\"paymentsInstrumentsId\":\"1BF5D57F-EB48-485D-B5CC-E3086FAA56B1\",\"creditCardType\":\"Visa\",\"nameOnCard\":\"adnskld dnalskdna\",\"expired\":\"false\",\"expirationDate\":\"2015-10\"}],\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"loyaltyMembershipInfo\":{\"loyaltyAccountNumber\":\"872010\",\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"bookingCurrency\":\"USD\",\"isAllowedToShopWithPoints\":true,\"loyaltyAmountAvailable\":42.00,\"loyaltyTierCredits\":{\"m_pointsAmount\":0,\"m_grossBookingValue\":{\"amount\":0.00,\"currency\":{\"currencyCode\":\"USD\"},\"uCurrency\":{\"isoCode\":\"USD\",\"type\":\"currency\",\"subType\":\"USD\"}},\"m_roomNights\":0,\"thisClass\":{}}},\"isSmokingPreferred\":false,\"tsaDetails\":{\"gender\":\"Unknown\"},\"seatPreference\":\"ANY\",\"specialAssistance\":\"NONE\",\"activityId\":\"66a68efa-a527-495c-95cb-e57f320d8a4f\"}"
    private val GOLD_STATUS_V2 = "{\"tuid\":673093611,\"expUserId\":45313772,\"email\":\"goldstatus@mobiata.com\",\"firstName\":\"Gold\",\"middleName\":\"\",\"lastName\":\"Status\",\"success\":true,\"detailedStatus\":\"Success\",\"detailedStatusMsg\":\"Already logged in\",\"phoneNumbers\":[{\"number\":\"6200790\",\"areaCode\":\"614\",\"category\":\"PRIMARY\",\"countryCode\":\"1\",\"extensionNumber\":\"\"}],\"storedCreditCards\":[{\"description\":\"Visa 1111\",\"paymentsInstrumentsId\":\"1BF5D57F-EB48-485D-B5CC-E3086FAA56B1\",\"creditCardType\":\"Visa\",\"nameOnCard\":\"adnskld dnalskdna\",\"expired\":\"false\",\"expirationDate\":\"2015-10\"}],\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"loyaltyMembershipInfo\":{\"loyaltyAccountNumber\":\"872010\",\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"bookingCurrency\":\"USD\",\"isAllowedToShopWithPoints\":true,\"loyaltyMonetaryValue\":{\"amount\":\"7743.14\",\"formattedPrice\":\"$7,743.14\",\"formattedWholePrice\":\"$7,743\",\"currencyCode\":\"USD\"}},\"isSmokingPreferred\":false,\"tsaDetails\":{\"gender\":\"Unknown\"},\"seatPreference\":\"ANY\",\"specialAssistance\":\"NONE\",\"activityId\":\"66a68efa-a527-495c-95cb-e57f320d8a4f\"}"
    private val STORED_PAYMENT_WITHOUT_TYPE = "{\"tuid\":673093611,\"expUserId\":45313772,\"email\":\"goldstatus@mobiata.com\",\"firstName\":\"Gold\",\"middleName\":\"\",\"lastName\":\"Status\",\"success\":true,\"detailedStatus\":\"Success\",\"detailedStatusMsg\":\"Already logged in\",\"phoneNumbers\":[{\"number\":\"6200790\",\"areaCode\":\"614\",\"category\":\"PRIMARY\",\"countryCode\":\"1\",\"extensionNumber\":\"\"}],\"storedCreditCards\":[{\"description\":\"ELV PAYMENT\",\"paymentsInstrumentsId\":\"1BF5D57F-EB48-485D-B5CC-E3086FAA56B1\"}],\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"loyaltyMembershipInfo\":{\"loyaltyAccountNumber\":\"872010\",\"loyaltyMembershipNumber\":\"312172038\",\"loyaltyMemebershipActive\":true,\"loyaltyPointsAvailable\":54206,\"loyaltyPointsPending\":5601,\"loyaltyMemebershipName\":\"REWARDS\",\"membershipTierName\":\"Gold\",\"bookingCurrency\":\"USD\",\"isAllowedToShopWithPoints\":true,\"loyaltyMonetaryValue\":{\"amount\":\"7743.14\",\"formattedPrice\":\"$7,743.14\",\"formattedWholePrice\":\"$7,743\",\"currencyCode\":\"USD\"}},\"isSmokingPreferred\":false,\"tsaDetails\":{\"gender\":\"Unknown\"},\"seatPreference\":\"ANY\",\"specialAssistance\":\"NONE\",\"activityId\":\"66a68efa-a527-495c-95cb-e57f320d8a4f\"}"

    lateinit var handler: SignInResponseHandler

    @Test
    fun v1ResponseParsesProperly() {
        givenResponseHandlerPrepared()

        val v1Json = JSONObject(GOLD_STATUS_V1)
        val response = handler.handleJson(v1Json)

        assertTrue(response.isSuccess)
        val user = response.user

        thenCommonValuesShouldBeCorrect(user)

        assertNull(user.loyaltyMembershipInformation?.loyaltyMonetaryValue?.formattedMoney)
    }

    @Test
    fun v2ResponseParsesProperly() {
        givenResponseHandlerPrepared()

        val v1Json = JSONObject(GOLD_STATUS_V2)
        val response = handler.handleJson(v1Json)

        assertTrue(response.isSuccess)
        val user = response.user

        thenCommonValuesShouldBeCorrect(user)

        assertEquals("$7,743.14", user.loyaltyMembershipInformation?.loyaltyMonetaryValue?.formattedMoney)
    }

    @Test
    fun testInvalidPaymentNotAddedToUserAccount() {
        givenResponseHandlerPrepared()
        val invalidCardJson = JSONObject(STORED_PAYMENT_WITHOUT_TYPE)
        val response = handler.handleJson(invalidCardJson)

        assertTrue(response.isSuccess)
        assertEquals(0, response.user.storedCreditCards.size)
    }

    private fun thenCommonValuesShouldBeCorrect(user: User) {
        assertEquals("673093611", user.tuidString)
        assertEquals("45313772", user.expediaUserId)
        assertEquals("goldstatus@mobiata.com", user.primaryTraveler.email)
        assertEquals("Gold Status", user.primaryTraveler.fullName)
        assertEquals("1", user.primaryTraveler.primaryPhoneNumber.countryCode)
        assertEquals("6146200790", user.primaryTraveler.primaryPhoneNumber.number)
        assertEquals(1, user.storedCreditCards.size)
        assertEquals(PaymentType.CARD_VISA, user.storedCreditCards[0].type)
        assertEquals("Visa 1111", user.storedCreditCards[0].description)
        assertEquals("1BF5D57F-EB48-485D-B5CC-E3086FAA56B1", user.storedCreditCards[0].id)
        assertEquals("adnskld dnalskdna", user.storedCreditCards[0].nameOnCard)
        assertFalse(user.storedCreditCards[0].isExpired)
        assertEquals(54206.0, user.loyaltyMembershipInformation?.loyaltyPointsAvailable)
        assertEquals(5601.0, user.loyaltyMembershipInformation?.loyaltyPointsPending)
        assertTrue(user.loyaltyMembershipInformation?.isLoyaltyMembershipActive ?: false)
        assertTrue(user.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false)
        assertEquals("USD", user.loyaltyMembershipInformation?.bookingCurrency)
        assertEquals(LoyaltyMembershipTier.TOP, user.loyaltyMembershipInformation?.loyaltyMembershipTier)
        assertNotNull(user.loyaltyMembershipInformation?.loyaltyMonetaryValue)
    }

    private fun givenResponseHandlerPrepared() {
        handler = SignInResponseHandler()
    }
}