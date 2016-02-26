package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.HotelSearch
import com.expedia.bookings.data.Rate
import com.expedia.bookings.data.TripBucketItemHotel
import com.expedia.bookings.tracking.FacebookEvents
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class FacebookEventsTest {

    @Test
    fun basicNulls() {
        FacebookEvents().trackHotelSearch(HotelSearch())
        FacebookEvents().trackHotelInfoSite(HotelSearch())
        FacebookEvents().trackHotelCheckout(TripBucketItemHotel(), Rate())
        FacebookEvents().trackHotelConfirmation(TripBucketItemHotel(), Rate())
    }
}
