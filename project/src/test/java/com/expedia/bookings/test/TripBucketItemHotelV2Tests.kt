package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class TripBucketItemHotelV2Tests {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Before
    fun before() {
        Db.getTripBucket().clear()
    }

    @Test
    fun updateHotelProductsWithoutCheckoutUserPreferences() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val checkoutPriceChangeResponse = mockHotelServiceTestRule.getPriceChangeCheckoutResponse()
        Db.getTripBucket().hotelV2.updateAfterCheckoutPriceChange(checkoutPriceChangeResponse)
        assertNull(createTripResponse.pointsDetails)
        assertNull(createTripResponse.userPreferencePoints)
    }

    @Test
    fun updateHotelProductsWithCheckoutUserPreferences() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val checkoutPriceChangeResponse = mockHotelServiceTestRule.getPriceChangeWithUserPreferencesCheckoutResponse()
        Db.getTripBucket().hotelV2.updateAfterCheckoutPriceChange(checkoutPriceChangeResponse)
        assertNotNull(createTripResponse.pointsDetails)
        assertNotNull(createTripResponse.userPreferencePoints)
    }
}
