package com.expedia.bookings.test

import android.app.Application
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelCheckoutSummaryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelCheckoutSummaryViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var sut: HotelCheckoutSummaryViewModel
    lateinit private var createTripResponse: HotelCreateTripResponse
    lateinit private var hotelProductResponse: HotelCreateTripResponse.HotelProductResponse
    lateinit private var context: Application

    @Before
    fun before() {
        context = RuntimeEnvironment.application
    }

    @Test
    fun happy() {
        givenHappyHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val hotelRoomResponse = hotelProductResponse.hotelRoomResponse
        val rate = hotelRoomResponse.rateInfo.chargeableRateInfo
        val expectedRateAdjustments = rate.getPriceAdjustments()
        val expectedHotelName = hotelProductResponse.getHotelName()

        assertFalse(sut.isPayLater.value)
        assertFalse(sut.isResortCase.value)
        assertFalse(sut.isPayLaterOrResortCase.value)
        assertEquals(expectedRateAdjustments, sut.priceAdjustments.value)
        assertEquals(expectedHotelName, sut.hotelName.value)
        assertEquals(hotelProductResponse.checkInDate, sut.checkInDate.value)
        assertEquals("Mar 22, 2013 - Mar 23, 2013", sut.checkInOutDatesFormatted.value)
        assertEquals(hotelProductResponse.hotelAddress, sut.address.value)
        assertEquals("San Francisco, CA", sut.city.value)
        assertEquals(hotelRoomResponse.roomTypeDescription, sut.roomDescriptions.value)
        assertEquals("1 Night", sut.numNights.value)
        assertEquals("1 Guest", sut.numGuests.value)
        assertEquals(hotelRoomResponse.hasFreeCancellation, sut.hasFreeCancellation.value)
        assertEquals(rate.currencyCode, sut.currencyCode.value)
        assertEquals(rate.nightlyRatesPerRoom, sut.nightlyRatesPerRoom.value)
        assertEquals(rate.nightlyRateTotal.toString(), sut.nightlyRateTotal.value)
        assertEquals(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode).formattedMoney, sut.surchargeTotalForEntireStay.value.formattedMoney)
        assertEquals(rate.taxStatusType, sut.taxStatusType.value)
        assertEquals(rate.extraGuestFees, sut.extraGuestFees.value)
        assertEquals(rate.displayTotalPrice.formattedMoney, sut.tripTotalPrice.value)
        assertEquals(Money(BigDecimal(rate.total.toDouble()), rate.currencyCode).formattedMoney, sut.dueNowAmount.value)
        assertFalse(sut.showFeesPaidAtHotel.value)
        assertEquals(Money(BigDecimal(rate.totalMandatoryFees.toString()), rate.currencyCode).formattedMoney, sut.feesPaidAtHotel.value)
        assertTrue(sut.isBestPriceGuarantee.value)
        assertEquals(sut, sut.newDataObservable.value)
    }

    @Test
    fun notPayLaterHoteldueNowIsTotalPrice() {
        givenHappyHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val expectedDueNow = "$" + hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total

        assertEquals(expectedDueNow, sut.dueNowAmount.value)
    }

    @Test
    fun payLaterHotelDueNowIsDepostitAmount() {
        givenPayLaterHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val expectedDueNow = "AUD" + hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.depositAmount

        assertEquals(expectedDueNow, sut.dueNowAmount.value)
    }

    @Test
    fun priceChangeUp() {
        givenPriceChangedUpResponse()
        setup()

        sut.tripResponseObserver.onNext(createTripResponse)
        assertEquals("Price changed from $2,394.88", sut.priceChangeMessage.value)
        assertEquals(R.drawable.price_change_increase, sut.priceChangeIconResourceId.value)
        assertTrue(sut.isPriceChange.value)
    }

    @Test
    fun priceChangeDown() {
        givenPriceChangedDownResponse()
        setup()

        sut.tripResponseObserver.onNext(createTripResponse)
        assertEquals("Price dropped from $2,394.88", sut.priceChangeMessage.value)
        assertEquals(R.drawable.price_change_decrease, sut.priceChangeIconResourceId.value)
        assertTrue(sut.isPriceChange.value)
    }

    private fun givenPriceChangedUpResponse() {
        createTripResponse = mockHotelServiceTestRule.getPriceChangeUpCreateTripResponse()
        hotelProductResponse = createTripResponse.originalHotelProductResponse
    }

    private fun givenPriceChangedDownResponse() {
        createTripResponse = mockHotelServiceTestRule.getPriceChangeDownCreateTripResponse()
        hotelProductResponse = createTripResponse.originalHotelProductResponse
    }

    private fun givenPayLaterHotelProductResponse() {
        createTripResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun givenHappyHotelProductResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun setup() {
        sut = HotelCheckoutSummaryViewModel(context)
    }
}
