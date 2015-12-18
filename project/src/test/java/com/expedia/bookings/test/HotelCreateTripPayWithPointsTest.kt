package com.expedia.bookings.test

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.ValidPayment
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class HotelCreateTripPayWithPointsTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePoints() {
        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        Assert.assertNotNull(createTripResponse.pointsDetails)
        Assert.assertTrue(createTripResponse.pointsDetails[0].isAllowedToRedeem)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].totalAvailable)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].maxPayableWithPoints)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].remainingPayableByCard)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].programName)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].paymentsInstrumentsId)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].rateID)
        Assert.assertTrue(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
        Assert.assertTrue(createTripResponse.pointsDetails[0].minimumPointsRequiredToRedeem > 0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithNonRedeemablePoints() {
        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemeblePointsCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        Assert.assertNotNull(createTripResponse.pointsDetails)
        Assert.assertFalse(createTripResponse.pointsDetails[0].isAllowedToRedeem)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].totalAvailable)
        Assert.assertNull(createTripResponse.pointsDetails[0].maxPayableWithPoints)
        Assert.assertNull(createTripResponse.pointsDetails[0].remainingPayableByCard)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].programName)
        Assert.assertTrue(createTripResponse.pointsDetails[0].minimumPointsRequiredToRedeem > 0)
        Assert.assertNotNull(createTripResponse.pointsDetails[0].rateID)
        Assert.assertFalse(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
    }

    @Test
    fun testCreateTripForGuestUsers() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        // No Pay with points details returned for guest users
        Assert.assertNull(createTripResponse.pointsDetails)
        Assert.assertFalse(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
    }

    @After
    fun cleanup() {
        Db.getTripBucket().clear()
    }
}