package com.expedia.bookings.test

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.payment.PointsProgramType
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
        Assert.assertNotNull(createTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS))
        val pointDetails = createTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!
        Assert.assertTrue(pointDetails.isAllowedToRedeem)
        Assert.assertNotNull(pointDetails.totalAvailable)
        Assert.assertNotNull(pointDetails.maxPayableWithPoints)
        Assert.assertNotNull(pointDetails.remainingPayableByCard)
        Assert.assertNotNull(pointDetails.programName)
        Assert.assertNotNull(pointDetails.paymentsInstrumentsId)
        Assert.assertNotNull(pointDetails.rateID)
        Assert.assertTrue(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
        Assert.assertTrue(pointDetails.minimumPointsRequiredToRedeem > 0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithNonRedeemablePoints() {
        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemeblePointsCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        Assert.assertNotNull(createTripResponse.pointsDetails)
        Assert.assertNotNull(createTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS))
        val pointDetails = createTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!
        Assert.assertFalse(pointDetails.isAllowedToRedeem)
        Assert.assertNotNull(pointDetails.totalAvailable)
        Assert.assertNull(pointDetails.maxPayableWithPoints)
        Assert.assertNull(pointDetails.remainingPayableByCard)
        Assert.assertNotNull(pointDetails.programName)
        Assert.assertTrue(pointDetails.minimumPointsRequiredToRedeem > 0)
        Assert.assertNotNull(pointDetails.rateID)
        Assert.assertFalse(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
    }

    @Test
    fun testCreateTripForGuestUsers() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        // No Pay with points details returned for guest users
        Assert.assertNull(createTripResponse.pointsDetails)
        Assert.assertFalse(ValidPayment.isPaymentTypeSupported(createTripResponse.validFormsOfPayment, PaymentType.POINTS_EXPEDIA_REWARDS))
        Assert.assertNull(createTripResponse.getPointDetails(PointsProgramType.EXPEDIA_REWARDS))
    }

    @After
    fun cleanup() {
        Db.getTripBucket().clear()
    }
}