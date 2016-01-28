package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class TripBucketItemHotelV2Tests {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Test
    fun updateHotelProductsWithoutCheckoutUserPreferences() {
        var createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        var checkoutPriceChangeResponse = mockHotelServiceTestRule.getPriceChangeCheckoutResponse()
        Db.getTripBucket().hotelV2.updateHotelProductsAfterCheckoutPriceChange(checkoutPriceChangeResponse)
        assertNull(createTripResponse.pointsDetails)
        assertNull(createTripResponse.userPreferencePoints)
    }

    @Test
    fun updateHotelProductsWithCheckoutUserPreferences() {
        var createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        var checkoutPriceChangeResponse = mockHotelServiceTestRule.getPriceChangeWithUserPreferencesCheckoutResponse()
        Db.getTripBucket().hotelV2.updateHotelProductsAfterCheckoutPriceChange(checkoutPriceChangeResponse)
        assertNotNull(createTripResponse.pointsDetails)
        assertNotNull(createTripResponse.userPreferencePoints)
    }

}