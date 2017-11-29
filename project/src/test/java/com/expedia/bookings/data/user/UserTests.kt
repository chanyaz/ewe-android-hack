package com.expedia.bookings.data.user

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class UserTests {
    @Test
    fun testVersionOneFromJSONEquality() {
        assertMemberEqualityForUser(User(UserJSONHelper.versionOneUserJSONObject))
    }

    @Test
    fun testVersionTwoFromJSONEquality() {
        assertMemberEqualityForUser(User(UserJSONHelper.versionTwoUserJSONObject))
    }

    @Test
    fun testToJSONEquality() {
        val user = UserLoginTestUtil.mockUser(LoyaltyMembershipTier.TOP)
        val loyaltyValue = LoyaltyMembershipTier.TOP.toApiValue()
        val tierField = if (loyaltyValue != null) ""","membershipTierName":"$loyaltyValue"""" else ""
        val membershipInfo = """{"isAllowedToShopWithPoints":false$tierField}"""
        val expectedJSON = """{"version":2,"loyaltyMembershipInformation":$membershipInfo}"""

        assertEquals(expectedJSON, user.toJson().toString())
    }

    @Test
    fun testExpiredCreditCard() {
        val userWithExpiredCreditCard = getUserWithOneCardAndExpired()
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.size)
    }

    @Test
    fun testMultipleCards() {
        val userWithExpiredCreditCard = getUserWithMultipleCardsIncludingExpired()
        assertEquals(4, userWithExpiredCreditCard.storedCreditCards.size)
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.filter { it.isExpired }.size)
    }

    private fun getUserWithOneCardAndExpired(): User {
        val user = User()
        addCreditCardToUser(user, true)
        return user
    }

    private fun getUserWithMultipleCardsIncludingExpired(): User {
        val user = User()
        addCreditCardToUser(user, true)
        for (i in 2..5) {
            addCreditCardToUser(user, false)
        }
        addCreditCardToUser(user, true)
        return user
    }

    private fun addCreditCardToUser(user: User, isExpired: Boolean) {
        user.addStoredCreditCard(getDummyStoredCreditCard(isExpired))
    }

    private fun getDummyStoredCreditCard(isExpired: Boolean): StoredCreditCard {
        val storedCreditCard = StoredCreditCard()
        storedCreditCard.isExpired = isExpired
        return storedCreditCard
    }

    private fun assertMemberEqualityForUser(user: User) {
        assertEquals(100, user.primaryTraveler.tuid)
        assertEquals(200, user.primaryTraveler.expediaUserId)
        assertEquals("Paul", user.primaryTraveler.firstName)
        assertEquals("Vincent", user.primaryTraveler.middleName)
        assertEquals("Kite", user.primaryTraveler.lastName)
        assertEquals("pkite@expedia.com", user.primaryTraveler.email)
        assertEquals("5555555555", user.primaryTraveler.phoneNumber)
        assertEquals("5555555555", user.primaryTraveler.primaryPhoneNumber.number)
        assertEquals("US", user.primaryTraveler.phoneCountryCode)
        assertEquals("United States", user.primaryTraveler.phoneCountryName)
        assertEquals("San Francisco", user.primaryTraveler.homeAddress.city)
        assertEquals("94104", user.primaryTraveler.homeAddress.postalCode)
        assertEquals("USA", user.primaryTraveler.homeAddress.countryCode)
        assertEquals("114 Sansome St", user.primaryTraveler.homeAddress.streetAddressLine1)
        assertEquals("", user.primaryTraveler.homeAddress.streetAddressLine2)
        assertEquals(1, user.storedCreditCards.size)
        assertEquals("4444444444444448", user.storedCreditCards.first().cardNumber)
        assertEquals("Visa", user.storedCreditCards.first().description)
        assertEquals("1", user.storedCreditCards.first().id)
        assertFalse(user.storedCreditCards.first().isExpired)
        assertFalse(user.storedCreditCards.first().isGoogleWallet)
        assertTrue(user.storedCreditCards.first().isSelectable)
        assertEquals("Paul Kite", user.storedCreditCards.first().nameOnCard)
        assertEquals(PaymentType.CARD_VISA, user.storedCreditCards.first().type)
        assertEquals(1, user.storedCreditCards.size)
        assertEquals("Visa", user.storedPointsCards.first().description)
        assertEquals("1", user.storedPointsCards.first().paymentsInstrumentId)
        assertEquals(PaymentType.CARD_VISA, user.storedPointsCards.first().paymentType)
        assertEquals(1, user.associatedTravelers.size)
        assertEquals("300", user.rewardsMembershipId)
        assertEquals(10000.0, user.loyaltyMembershipInformation?.loyaltyPointsAvailable)
        assertEquals(1000.0, user.loyaltyMembershipInformation?.loyaltyPointsPending)
        assertEquals("USD", user.loyaltyMembershipInformation?.bookingCurrency)
        assertTrue(user.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false)
        assertTrue(user.loyaltyMembershipInformation?.isLoyaltyMembershipActive ?: false)
        assertEquals(LoyaltyMembershipTier.TOP.toApiValue(), user.loyaltyMembershipInformation?.loyaltyMembershipTier?.toApiValue())
        assertEquals(100.0, user.loyaltyMembershipInformation?.loyaltyMonetaryValue?.amount?.toDouble())
        assertEquals("USD", user.loyaltyMembershipInformation?.loyaltyMonetaryValue?.currencyCode)
    }
}
