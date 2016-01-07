package com.expedia.bookings.unit

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.PointsAndCurrency
import com.expedia.bookings.data.hotels.PointsDetails
import com.expedia.bookings.data.hotels.PointsProgramType
import com.expedia.bookings.data.hotels.PointsType
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList

public class HotelCreateTripResponseTest {

    @Test
    fun testGetPointDetailsWhenExpediaRewardsNotAvailable() {
        val hotelCreateTripResponse = HotelCreateTripResponse()
        Assert.assertNull(hotelCreateTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS))
    }

    @Test
    fun testGetPointDetailsWhenExpediaRewardsAvailable() {
        val hotelCreateTripResponse = HotelCreateTripResponse()
        hotelCreateTripResponse.pointsDetails = ArrayList()
        hotelCreateTripResponse.pointsDetails.add(PointsDetails(PointsProgramType.EXPEDIA_REWARDS, true, 0, PointsAndCurrency(0, PointsType.BURN, Money()), null, null, null, ""))
        Assert.assertNotNull(hotelCreateTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS))
        Assert.assertEquals(hotelCreateTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS).programName, PointsProgramType.EXPEDIA_REWARDS)
    }
}