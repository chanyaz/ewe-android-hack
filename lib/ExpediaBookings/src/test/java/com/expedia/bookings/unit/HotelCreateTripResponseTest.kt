package com.expedia.bookings.unit

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsDetails
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList

class HotelCreateTripResponseTest {

    @Test
    fun testGetPointDetailsWhenExpediaRewardsNotAvailable() {
        val hotelCreateTripResponse = HotelCreateTripResponse()
        Assert.assertNull(hotelCreateTripResponse.getPointDetails())
    }

    @Test
    fun testGetPointDetailsWhenExpediaRewardsAvailable() {
        val hotelCreateTripResponse = HotelCreateTripResponse()
        val pointDetails = ArrayList<PointsDetails>()
        pointDetails.add(PointsDetails(ProgramName.ExpediaRewards, true, 0, PointsAndCurrency(0f, PointsType.BURN, Money()), null, null, null, null, ""))
        hotelCreateTripResponse.pointsDetails = pointDetails
        Assert.assertNotNull(hotelCreateTripResponse.getPointDetails())
        Assert.assertEquals(hotelCreateTripResponse.getPointDetails()!!.programName, ProgramName.ExpediaRewards)
    }
}
