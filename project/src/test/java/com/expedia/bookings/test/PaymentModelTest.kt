package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.util.notNullAndObservable
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.expedia.bookings.services.TestObserver
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class PaymentModelTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    var paymentModel: PaymentModel<HotelCreateTripResponse> by notNullAndObservable {
        it.createTripSubject.subscribe(createTripResponseTestObserver)
        it.paymentSplits.subscribe(paymentSplitsTestObserver)
        it.burnAmountSubject.subscribe(amountChosenToBePaidWithPointsTestObserver)
        it.burnAmountToPointsApiResponse.subscribe(currencyToPointsApiResponseTestObserver)
        it.burnAmountToPointsApiError.subscribe(currencyToPointsApiErrorTestObserver)
        it.couponChangeSubject.subscribe(couponChangeTestObserver)
        it.priceChangeDuringCheckoutSubject.subscribe(priceChangeDuringCheckoutTestObserver)
    }
    var createTripResponse: HotelCreateTripResponse by Delegates.notNull()

    val amountChosenToBePaidWithPointsTestObserver = TestObserver<BigDecimal>()
    val paymentSplitsTestObserver = TestObserver<PaymentSplits>()
    val currencyToPointsApiResponseTestObserver = TestObserver<CalculatePointsResponse>()
    val currencyToPointsApiErrorTestObserver = TestObserver<ApiError>()
    val createTripResponseTestObserver = TestObserver<TripResponse>()
    val couponChangeTestObserver = TestObserver<TripResponse>()
    val priceChangeDuringCheckoutTestObserver = TestObserver<TripResponse>()

    //TODO Mock data for price change and coupon change does not have points in response similar to create trip
    //so leaving the tests for these for now and created mingle card for the same
    //https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/6016
    //val priceChangeDuringCheckoutResponseTestObserver = ExtendedTestObserver<HotelCreateTripResponse>()
    //val couponChangeResponseTestObserver = ExtendedTestObserver<HotelCreateTripResponse>()

    private fun setupCreateTrip(hasRedemablePoints: Boolean) {
        if (hasRedemablePoints)
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        else
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemablePointsCreateTripResponse()

        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    private fun setupApplyCoupon() {
        createTripResponse = mockHotelServiceTestRule.getApplyCouponResponseWithUserPreference()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    private fun setupCheckout(withUserPreferences: Boolean) {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val checkoutResponse = if (withUserPreferences) mockHotelServiceTestRule.getPriceChangeWithUserPreferencesCheckoutResponse() else mockHotelServiceTestRule.getPriceChangeCheckoutResponse()
        Db.getTripBucket().hotelV2.updateAfterCheckoutPriceChange(checkoutResponse)
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePoints() {
        setupCreateTrip(true)
        val expediaPointDetails = createTripResponse.getPointDetails()

        //Expected Payment Split
        var expectedPaymentSplits = PaymentSplits(expediaPointDetails!!.maxPayableWithPoints!!, expediaPointDetails.remainingPayableByCard!!)
        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestObserver.assertNoErrors()
        createTripResponseTestObserver.assertValueCount(1)
        createTripResponseTestObserver.assertValue(createTripResponse)

        //When SWP Opted is true
        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        //Expected Payment Split when SWP Opted is false
        paymentModel.swpOpted.onNext(false)
        paymentModel.createTripSubject.onNext(createTripResponse)
        expectedPaymentSplits = getPaymentSplitsForSwpOff()

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(2)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[1], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsZeroCurrencyToPointSelected() {
        setupCreateTrip(true)
        //Expected Payment Split
        val expectedPaymentSplits = getPaymentSplitsForSwpOff()

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestObserver.assertNoErrors()
        createTripResponseTestObserver.assertValueCount(1)
        createTripResponseTestObserver.assertValue(createTripResponse)

        paymentModel.burnAmountSubject.onNext(BigDecimal.ZERO)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(2)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[1], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsCurrencyToPointUserSelected() {
        setupCreateTrip(true)
        val expectedPaymentSplits = PaymentSplits(PointsAndCurrency(14005f, PointsType.BURN, Money("100", "USD")),
                PointsAndCurrency(507f, PointsType.EARN, Money("3.7", "USD")))

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestObserver.assertNoErrors()
        createTripResponseTestObserver.assertValueCount(1)
        createTripResponseTestObserver.assertValue(createTripResponse)

        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(1)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(2)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[1], expectedPaymentSplits))
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsCurrencyToPointUserSelectedAPIError() {
        setupCreateTrip(true)
        createTripResponse.tripId = "";

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestObserver.assertNoErrors()
        createTripResponseTestObserver.assertValueCount(1)
        createTripResponseTestObserver.assertValue(createTripResponse)

        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(1)

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
    }

    @Test
    fun testCreateTripForLoggedInUserWithNonRedeemablePoints() {
        setupCreateTrip(false)

        //Expected Payment Split
        val expectedPaymentSplits = getPaymentSplitsForSwpOff()

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestObserver.assertNoErrors()
        createTripResponseTestObserver.assertValueCount(1)
        createTripResponseTestObserver.assertValue(createTripResponse)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    @Test
    fun testApplyCouponWithUserPreference() {
        setupApplyCoupon()
        val userPreference = createTripResponse.userPreferencePoints

        //Expected Payment Split
        val expectedPaymentSplits = PaymentSplits(userPreference!!.getUserPreference(ProgramName.ExpediaRewards)!!, userPreference.remainingPayableByCard)

        //User has PwP opted
        paymentModel.pwpOpted.onNext(true)

        paymentModel.couponChangeSubject.onNext(createTripResponse)
        couponChangeTestObserver.assertNoErrors()
        couponChangeTestObserver.assertValueCount(1)
        couponChangeTestObserver.assertValue(createTripResponse)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    @Test
    fun testApplyCouponWithUserPreferenceIfSwpIsOff() {
        setupApplyCoupon()

        paymentModel.swpOpted.onNext(false)
        paymentModel.pwpOpted.onNext(false)
        paymentModel.couponChangeSubject.onNext(createTripResponse)
        couponChangeTestObserver.assertNoErrors()
        couponChangeTestObserver.assertValueCount(1)
        couponChangeTestObserver.assertValue(createTripResponse)

        //Expected Payment Split when SWP Opted is false
        val expectedPaymentSplits = getPaymentSplitsForSwpOff()

        Assert.assertFalse(paymentModel.pwpOpted.value)
        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)

        paymentModel.pwpOpted.onNext(true)

    }

    @Test
    fun testCheckoutPriceChangeWithUserPreference() {
        setupCheckout(true)
        val userPreference = createTripResponse.userPreferencePoints

        paymentModel.pwpOpted.onNext(true)
        //Expected Payment Split
        val expectedPaymentSplits = PaymentSplits(userPreference!!.getUserPreference(ProgramName.ExpediaRewards)!!, userPreference.remainingPayableByCard)

        paymentModel.priceChangeDuringCheckoutSubject.onNext(createTripResponse)
        priceChangeDuringCheckoutTestObserver.assertNoErrors()
        priceChangeDuringCheckoutTestObserver.assertValueCount(1)
        priceChangeDuringCheckoutTestObserver.assertValue(createTripResponse)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    @Test
    fun testCheckoutPriceChangeWithoutUserPreference() {
        setupCheckout(false)

        paymentModel.pwpOpted.onNext(true)
        //Expected Payment Split
        val expectedPaymentSplits = getPaymentSplitsForSwpOff()

        paymentModel.priceChangeDuringCheckoutSubject.onNext(createTripResponse)
        priceChangeDuringCheckoutTestObserver.assertNoErrors()
        priceChangeDuringCheckoutTestObserver.assertValueCount(1)
        priceChangeDuringCheckoutTestObserver.assertValue(createTripResponse)

        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }



    @Test
    fun testCheckoutPriceChangeWithUserPreferenceWhenSwpisOff() {
        setupCheckout(true)

        paymentModel.swpOpted.onNext(false)
        paymentModel.pwpOpted.onNext(false)
        paymentModel.priceChangeDuringCheckoutSubject.onNext(createTripResponse)
        priceChangeDuringCheckoutTestObserver.assertNoErrors()
        priceChangeDuringCheckoutTestObserver.assertValueCount(1)
        priceChangeDuringCheckoutTestObserver.assertValue(createTripResponse)

        //Expected Payment Split when SWP Opted is false
        val expectedPaymentSplits = getPaymentSplitsForSwpOff()
        Assert.assertFalse(paymentModel.pwpOpted.value)
        paymentSplitsTestObserver.assertNoErrors()
        paymentSplitsTestObserver.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestObserver.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestObserver.assertNoErrors()
        currencyToPointsApiResponseTestObserver.assertValueCount(0)

        currencyToPointsApiErrorTestObserver.assertNoErrors()
        currencyToPointsApiErrorTestObserver.assertValueCount(0)
    }

    private fun comparePaymentSplits(paymentSplits: PaymentSplits, expectedPaymentSplits: PaymentSplits): Boolean {
        return paymentSplits.payingWithCards.amount.compareTo(expectedPaymentSplits.payingWithCards.amount) == 0 &&
                paymentSplits.payingWithCards.points.equals(expectedPaymentSplits.payingWithCards.points) &&
                paymentSplits.payingWithCards.pointsType.equals(expectedPaymentSplits.payingWithCards.pointsType) &&
                paymentSplits.payingWithPoints.amount.compareTo(expectedPaymentSplits.payingWithPoints.amount) == 0 &&
                paymentSplits.payingWithPoints.points.equals(expectedPaymentSplits.payingWithPoints.points) &&
                paymentSplits.payingWithPoints.pointsType.equals(expectedPaymentSplits.payingWithPoints.pointsType)
    }

    private fun getPaymentSplitsForSwpOff(): PaymentSplits{
        val payingWithPoints = PointsAndCurrency(0f, PointsType.BURN, Money("0", createTripResponse.getTripTotalExcludingFee().currencyCode))
        val payingWithCards = PointsAndCurrency(createTripResponse.rewards?.totalPointsToEarn ?: 0f, PointsType.EARN, createTripResponse.getTripTotalExcludingFee())
        return PaymentSplits(payingWithPoints, payingWithCards)
    }
    //TODO unsubscribe of currencyToPointsApiSubscription can be tested.

    @After
    fun cleanup() {
        Db.getTripBucket().clear()
    }
}
