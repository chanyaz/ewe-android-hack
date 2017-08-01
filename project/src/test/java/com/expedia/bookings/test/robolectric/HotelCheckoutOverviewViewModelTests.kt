package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCheckoutOverviewViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelCheckoutOverviewViewModelTest {

    val mockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    lateinit var hotelProductResponse: HotelCreateTripResponse.HotelProductResponse
    lateinit var hotelcreateTripResponse: HotelCreateTripResponse
    lateinit var sut: HotelCheckoutOverviewViewModel
    lateinit private var paymentModel: PaymentModel<HotelCreateTripResponse>

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        sut = HotelCheckoutOverviewViewModel(activity, paymentModel)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun happyTest() {
        givenHappyCreateTripResponse()
        sut.newRateObserver.onNext(hotelProductResponse)
        paymentModel.createTripSubject.onNext(hotelcreateTripResponse)

        assertEquals("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, and the Privacy Policy.", sut.legalTextInformation.value.toString())
        assertEquals("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, and the Privacy Policy. Button", sut.legalTextInformationContentDesc.value.toString())
        assertEquals("The total for your trip will be $135.81. You'll pay the hotel the total cost of your booking (in the hotel's local currency) during your stay.", sut.disclaimerText.value.toString())
        assertEquals("Slide to reserve", sut.slideToText.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun rateIsShowResortFeeMessage() {
        givenHotelMustShowResortFee()
        sut.newRateObserver.onNext(hotelProductResponse)
        paymentModel.createTripSubject.onNext(hotelcreateTripResponse)

        assertEquals("The $0 resort fee will be collected at the hotel. The total price for your stay will be $135.81.", sut.disclaimerText.value.toString())
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun totalPriceCharged() {
        val totalPriceChargedSubscriber = TestObserver.create<String>()
        val paymentSplitsSubscriber = TestObserver.create<PaymentSplits>()
        sut.priceAboveSlider.subscribe(totalPriceChargedSubscriber)
        sut.paymentModel.paymentSplits.subscribe(paymentSplitsSubscriber)

        sut.paymentModel.createTripSubject.onNext(getCreateTripResponse(true))
        sut.newRateObserver.onNext(hotelProductResponse)
        paymentSplitsSubscriber.assertValueCount(1)

        sut.paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        paymentSplitsSubscriber.assertValueCount(2)

        sut.paymentModel.createTripSubject.onNext(getCreateTripResponse(true))
        paymentSplitsSubscriber.assertValueCount(3)

        //When user chooses to pay through card and reward points
        val latch = CountDownLatch(1)
        sut.paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        sut.paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        paymentSplitsSubscriber.assertValueCount(4)

        totalPriceChargedSubscriber.assertValues("You are using 2,500 ($1,000.00) Expedia+ points", "Your card will be charged $135.81", "You are using 2,500 ($1,000.00) Expedia+ points" ,"You are using 14,005 ($100.00) Expedia+ points\nYour card will be charged $3.70")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun roomIsPayLater() {
        givenHotelIsPayLater()
        sut.newRateObserver.onNext(hotelProductResponse)
        paymentModel.createTripSubject.onNext(hotelcreateTripResponse)

        assertEquals("Slide to reserve", sut.slideToText.value)
        assertEquals("You will be charged deposits by the property based on the following schedule. Any remaining amount will be due upon arrival: First night (after booking) ", sut.depositPolicyText.value.toString())
        assertEquals("The total for your trip will be $135.81. You'll pay the hotel the total cost of your booking (in the hotel's local currency) during your stay.", sut.disclaimerText.value.toString())
    }

    private fun givenHotelIsPayLater() {
        givenHappyCreateTripResponse()
        hotelProductResponse.hotelRoomResponse.isPayLater = true
    }

    private fun givenHotelMustShowResortFee() {
        givenHappyCreateTripResponse()
        hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.showResortFeeMessage = true
    }

    private fun givenHappyCreateTripResponse() {
        hotelcreateTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        hotelProductResponse = hotelcreateTripResponse.newHotelProductResponse
    }

    private fun getCreateTripResponse(hasRedeemablePoints: Boolean): HotelCreateTripResponse {
        val createTripResponse: HotelCreateTripResponse
        if (hasRedeemablePoints)
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        else
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemablePointsCreateTripResponse()

        createTripResponse.tripId = "happy"
        hotelProductResponse = createTripResponse.newHotelProductResponse
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

       return createTripResponse
    }
}
