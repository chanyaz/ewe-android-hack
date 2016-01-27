package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.util.notNullAndObservable
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

public class PaymentModelTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule<LoyaltyServices>(LoyaltyServices::class.java)
        @Rule get

    var paymentModel: PaymentModel<HotelCreateTripResponse> by notNullAndObservable {
        it.createTripSubject.subscribe(createTripResponseTestSubscriber)
        it.paymentSplits.subscribe(paymentSplitsTestSubscriber)
        it.amountChosenToBePaidWithPointsSubject.subscribe(amountChosenToBePaidWithPointsTestSubscriber)
        it.currencyToPointsApiResponse.subscribe(currencyToPointsApiResponseTestSubscriber)
        it.currencyToPointsApiError.subscribe(currencyToPointsApiErrorTestSubscriber)
        it.couponChangeSubject.subscribe(couponChangeTestSubscriber)
    }
    var createTripResponse: HotelCreateTripResponse by Delegates.notNull()

    val amountChosenToBePaidWithPointsTestSubscriber = TestSubscriber<BigDecimal>()
    val paymentSplitsTestSubscriber = TestSubscriber<PaymentSplits>()
    val currencyToPointsApiResponseTestSubscriber = TestSubscriber<CalculatePointsResponse>()
    val currencyToPointsApiErrorTestSubscriber = TestSubscriber<ApiError>()
    val createTripResponseTestSubscriber = TestSubscriber<TripResponse>()
    val couponChangeTestSubscriber = TestSubscriber<TripResponse>()

    //TODO Mock data for price change and coupon change does not have points in response similar to create trip
    //so leaving the tests for these for now and created mingle card for the same
    //https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/6016
    //val priceChangeDuringCheckoutResponseTestSubscriber = ExtendedTestSubscriber<HotelCreateTripResponse>()
    //val couponChangeResponseTestSubscriber = ExtendedTestSubscriber<HotelCreateTripResponse>()

    private fun setupCreateTrip(hasRedemablePoints: Boolean) {
        if (hasRedemablePoints)
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        else
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemeblePointsCreateTripResponse()

        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    private fun setupApplyCoupon() {
        createTripResponse = mockHotelServiceTestRule.getApplyCouponResponseWithUserPreference()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePoints() {
        setupCreateTrip(true)
        val expediaPointDetails = createTripResponse.getPointDetails(ProgramName.ExpediaRewards)

        //Expected Payment Split
        val expectedPaymentSplits = PaymentSplits(expediaPointDetails!!.maxPayableWithPoints!!, expediaPointDetails!!.remainingPayableByCard!!)
        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestSubscriber.assertNoErrors()
        createTripResponseTestSubscriber.assertValueCount(1)
        createTripResponseTestSubscriber.assertValue(createTripResponse)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents.get(0), expectedPaymentSplits))

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(0)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsZeroCurrencyToPointSelected() {
        setupCreateTrip(true)
        //Expected Payment Split
        val payingWithPoints = PointsAndCurrency(0, PointsType.BURN, Money("0", createTripResponse.getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(createTripResponse.expediaRewards!!.totalPointsToEarn, PointsType.EARN, createTripResponse.getTripTotal())
        val expectedPaymentSplits = PaymentSplits(payingWithPoints, payingWithCards)

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestSubscriber.assertNoErrors()
        createTripResponseTestSubscriber.assertValueCount(1)
        createTripResponseTestSubscriber.assertValue(createTripResponse)

        paymentModel.amountChosenToBePaidWithPointsSubject.onNext(BigDecimal.ZERO)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(2)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents.get(1), expectedPaymentSplits))

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(0)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsCurrencyToPointUserSelected() {
        setupCreateTrip(true)
        val expectedPaymentSplits = PaymentSplits(PointsAndCurrency(14005, PointsType.BURN, Money("100", "USD")),
                PointsAndCurrency(507, PointsType.EARN, Money("3.7", "USD")))

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestSubscriber.assertNoErrors()
        createTripResponseTestSubscriber.assertValueCount(1)
        createTripResponseTestSubscriber.assertValue(createTripResponse)

        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.amountChosenToBePaidWithPointsSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(1)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(0)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(2)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.getOnNextEvents().get(1), expectedPaymentSplits))
    }

    @Test
    fun testCreateTripForLoggedInUserWithRedeemablePointsCurrencyToPointUserSelectedAPIError() {
        setupCreateTrip(true)
        createTripResponse.tripId = "";

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestSubscriber.assertNoErrors()
        createTripResponseTestSubscriber.assertValueCount(1)
        createTripResponseTestSubscriber.assertValue(createTripResponse)

        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        paymentModel.amountChosenToBePaidWithPointsSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(1)

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(0)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(1)
    }

    @Test
    fun testCreateTripForLoggedInUserWithNonRedeemablePoints() {
        setupCreateTrip(false)

        //Expected Payment Split
        val payingWithPoints = PointsAndCurrency(0, PointsType.BURN, Money("0", createTripResponse.getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(createTripResponse.expediaRewards!!.totalPointsToEarn, PointsType.EARN, createTripResponse.getTripTotal())
        val expectedPaymentSplits = PaymentSplits(payingWithPoints, payingWithCards)

        paymentModel.createTripSubject.onNext(createTripResponse)
        createTripResponseTestSubscriber.assertNoErrors()
        createTripResponseTestSubscriber.assertValueCount(1)
        createTripResponseTestSubscriber.assertValue(createTripResponse)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents.get(0), expectedPaymentSplits))

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(0)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testApplyCouponWithUserPreference() {
        setupApplyCoupon()
        val userPreference = createTripResponse.userPreferencePoints

        //Expected Payment Split
        val expectedPaymentSplits = PaymentSplits(userPreference!!.getUserPreference(ProgramName.ExpediaRewards)!!, userPreference.remainingPayableByCard)

        paymentModel.couponChangeSubject.onNext(createTripResponse)
        couponChangeTestSubscriber.assertNoErrors()
        couponChangeTestSubscriber.assertValueCount(1)
        couponChangeTestSubscriber.assertValue(createTripResponse)

        paymentSplitsTestSubscriber.assertNoErrors()
        paymentSplitsTestSubscriber.assertValueCount(1)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents[0], expectedPaymentSplits))

        currencyToPointsApiResponseTestSubscriber.assertNoErrors()
        currencyToPointsApiResponseTestSubscriber.assertValueCount(0)

        currencyToPointsApiErrorTestSubscriber.assertNoErrors()
        currencyToPointsApiErrorTestSubscriber.assertValueCount(0)
    }

    private fun comparePaymentSplits(paymentSplits: PaymentSplits, expectedPaymentSplits: PaymentSplits): Boolean {
        return paymentSplits.payingWithCards.amount.compareTo(expectedPaymentSplits.payingWithCards.amount) == 0 &&
                paymentSplits.payingWithCards.points.equals(expectedPaymentSplits.payingWithCards.points) &&
                paymentSplits.payingWithCards.pointsType.equals(expectedPaymentSplits.payingWithCards.pointsType) &&
                paymentSplits.payingWithPoints.amount.compareTo(expectedPaymentSplits.payingWithPoints.amount) == 0 &&
                paymentSplits.payingWithPoints.points.equals(expectedPaymentSplits.payingWithPoints.points) &&
                paymentSplits.payingWithPoints.pointsType.equals(expectedPaymentSplits.payingWithPoints.pointsType)
    }

    //TODO unsubscribe of currencyToPointsApiSubscription can be tested.

    @After
    fun cleanup() {
        Db.getTripBucket().clear()
    }
}
