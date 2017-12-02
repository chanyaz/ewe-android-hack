package com.expedia.bookings.data.user

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class UserLoyaltyMembershipInformationTests {
    private val information: UserLoyaltyMembershipInformation
        get() {
            val info = UserLoyaltyMembershipInformation()
            info.bookingCurrency = "USD"
            info.isAllowedToShopWithPoints = true
            info.isLoyaltyMembershipActive = true

            val membershipTier = LoyaltyMembershipTier.TOP

            info.loyaltyMembershipTier = if (membershipTier.toApiValue() != null) membershipTier else LoyaltyMembershipTier.NONE

            val money = Money(100, "USD")

            info.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(money)
            info.loyaltyMonetaryValue.setApiFormattedPrice("")
            info.loyaltyPointsAvailable = 10000.0
            info.loyaltyPointsPending = 1000.0

            return info
        }

    @Test
    fun testFromJSONEquality() {
        val expectedInformation = information
        val informationFromJSON = UserLoyaltyMembershipInformation()
        informationFromJSON.fromJson(UserJSONHelper.userLoyaltyMembershipInformationJSONObject)

        assertEquals(expectedInformation.loyaltyPointsAvailable, informationFromJSON.loyaltyPointsAvailable)
        assertEquals(expectedInformation.loyaltyPointsPending, informationFromJSON.loyaltyPointsPending)
        assertEquals(expectedInformation.bookingCurrency, informationFromJSON.bookingCurrency)
        assertEquals(expectedInformation.isAllowedToShopWithPoints, informationFromJSON.isAllowedToShopWithPoints)
        assertEquals(expectedInformation.isLoyaltyMembershipActive, informationFromJSON.isLoyaltyMembershipActive)
        assertEquals(expectedInformation.loyaltyMembershipTier, informationFromJSON.loyaltyMembershipTier)
        assertEquals(expectedInformation.loyaltyMonetaryValue, informationFromJSON.loyaltyMonetaryValue)
    }

    @Test
    fun testToJSONEquality() {
        val expectedJSON = UserJSONHelper.userLoyaltyMembershipInformationJSONObject.toString()
        assertEquals(expectedJSON, information.toJson().toString())
    }
}
