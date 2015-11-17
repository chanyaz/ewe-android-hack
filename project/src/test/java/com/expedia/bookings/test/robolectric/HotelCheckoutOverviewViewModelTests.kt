package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.vm.HotelCheckoutOverviewViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.text.DecimalFormat
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelCheckoutOverviewViewModelTest {

    val mockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit var context: Context
    lateinit var hotelProductResponse: HotelCreateTripResponse.HotelProductResponse
    lateinit var sut: HotelCheckoutOverviewViewModel

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        context = activity.application

        sut = HotelCheckoutOverviewViewModel(context)
    }

    @Test
    fun happyTest() {
        givenHappyCreateTripResponse()
        sut.newRateObserver.onNext(hotelProductResponse)

        assertEquals("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, and the Privacy Policy.", sut.legalTextInformation.value.toString())
        assertEquals("", sut.disclaimerText.value.toString())
        assertEquals("Slide to purchase", sut.slideToText.value)
        assertEquals("Your card will be charged $135.81", sut.totalPriceCharged.value)
        assertEquals(Unit, sut.resetMenuButton.value)
    }

    @Test
    fun rateIsShowResortFeeMessage() {
        givenHotelMustShowResortFee()
        sut.newRateObserver.onNext(hotelProductResponse)

        assertEquals("The $0 resort fee will be collected at the hotel. The total price for your stay will be $135.81.", sut.disclaimerText.value.toString())
    }

    @Test
    fun roomIsPayLater() {
        givenHotelIsPayLater()
        sut.newRateObserver.onNext(hotelProductResponse)

        assertEquals("Slide to reserve", sut.slideToText.value)
        assertEquals("The total for your trip will be $135.81. You'll pay the hotel the total cost of your booking (in the hotel's local currency) during your stay.", sut.disclaimerText.value.toString())
    }

    @Test
    fun roomIsPayLaterAndDepositAmountToShowUsers() {
        val amountToShow = 42.0
        givenHotelIsPayLater()
        givenHotelHasDepositAmountToShowUsers(amountToShow)
        sut.newRateObserver.onNext(hotelProductResponse)

        assertEquals("Slide to reserve", sut.slideToText.value)
        assertEquals("A deposit of $42 will be collected now. You'll pay the hotel the remaining cost of your booking (in the hotel's local currency) during your stay.", sut.disclaimerText.value.toString())
    }

    private fun givenHotelIsPayLater() {
        givenHappyCreateTripResponse()
        hotelProductResponse.hotelRoomResponse.isPayLater = true
    }

    private fun givenHotelHasDepositAmountToShowUsers(amountToShow: Double) {
        val df = DecimalFormat("#")

        hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.depositAmount = df.format(amountToShow).toString()
    }

    private fun givenHotelMustShowResortFee() {
        givenHappyCreateTripResponse()
        hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.showResortFeeMessage = true
    }

    private fun givenHappyCreateTripResponse() {
        hotelProductResponse = mockHotelServiceTestRule.getHappyCreateTripResponse().newHotelProductResponse
    }
}
