package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.vm.HotelCheckoutSummaryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class HotelCheckoutSummaryViewModelTest {

    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var sut: HotelCheckoutSummaryViewModel
    lateinit private var hotelProductResponse: HotelCreateTripResponse.HotelProductResponse
    lateinit private var context: Context
    lateinit private var resources: Resources

    @Before
    fun before() {
        context = Mockito.mock(Context::class.java)
        resources = Mockito.mock(Resources::class.java)
    }

    @Test
    fun happy() {
        val expectedCheckInOutDatesFormatted = "xyz"
        val expectedCity = "abc"
        val expectedNumberNights = "123"
        val expectedNumberGuests = "321"
        givenResources(expectedCheckInOutDatesFormatted, expectedCity, expectedNumberNights, expectedNumberGuests)
        givenHappyHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val hotelRoomResponse = hotelProductResponse.hotelRoomResponse
        val rate = hotelRoomResponse.rateInfo.chargeableRateInfo
        val expectedRateAdjustments = rate.getPriceAdjustments()
        val expectedHotelName = hotelProductResponse.localizedHotelName

        assertFalse(sut.isPayLater.value)
        assertFalse(sut.isResortCase.value)
        assertFalse(sut.isPayLaterOrResortCase.value)
        assertEquals(expectedRateAdjustments, sut.priceAdjustments.value)
        assertEquals(expectedHotelName, sut.hotelName.value)
        assertEquals(hotelProductResponse.checkInDate, sut.checkInDate.value)
        assertEquals(expectedCheckInOutDatesFormatted, sut.checkInOutDatesFormatted.value)
        assertEquals(hotelProductResponse.hotelAddress, sut.address.value)
        assertEquals(expectedCity, sut.city.value)
        assertEquals(hotelRoomResponse.roomTypeDescription, sut.roomDescriptions.value)
        assertEquals(expectedNumberNights, sut.numNights.value)
        assertEquals(expectedNumberGuests, sut.numGuests.value)
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
        givenResources("", "", "", "")
        givenHappyHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val expectedDueNow = "$" + hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total

        assertEquals(expectedDueNow, sut.dueNowAmount.value)
    }

    @Test
    fun payLaterHotelDueNowIsDepostitAmount() {
        givenResources("", "", "", "")
        givenPayLaterHotelProductResponse()
        setup()

        sut.newRateObserver.onNext(hotelProductResponse)
        val expectedDueNow = "AUD" + hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.depositAmount

        assertEquals(expectedDueNow, sut.dueNowAmount.value)
    }

    @Test
    fun priceChangeUp() {
        val expectedPriceChangeMsg = "foo"
        givenResources("", "", "", "")
        givenPriceChangedUpResponse()
        givenPriceChangeMessage(expectedPriceChangeMsg)
        setup()

        sut.originalRateObserver.onNext(hotelProductResponse)
        assertEquals(expectedPriceChangeMsg, sut.priceChange.value)
        assertTrue(sut.isPriceChange.value)
    }

    @Test
    fun priceChangeDown() {
        val expectedPriceChangeMsg = "bar"
        givenResources("", "", "", "")
        givenPriceChangedDownResponse()
        givenPriceChangeMessage(expectedPriceChangeMsg)
        setup()

        sut.originalRateObserver.onNext(hotelProductResponse)
        assertEquals(expectedPriceChangeMsg, sut.priceChange.value)
        assertTrue(sut.isPriceChange.value)
    }

    private fun givenPriceChangedUpResponse() {
        hotelProductResponse = mockHotelServiceTestRule.getPriceChangeUpCreateTripResponse().originalHotelProductResponse
    }

    private fun givenPriceChangedDownResponse() {
        hotelProductResponse = mockHotelServiceTestRule.getPriceChangeDownCreateTripResponse().originalHotelProductResponse
    }

    private fun givenPayLaterHotelProductResponse() {
        hotelProductResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse().newHotelProductResponse
    }

    private fun givenHappyHotelProductResponse() {
        hotelProductResponse = mockHotelServiceTestRule.getHappyCreateTripResponse().newHotelProductResponse
    }

    private fun givenPriceChangeMessage(resultingMessage: String) {
        val originalPrice = Money(BigDecimal(hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()), "USD").formattedMoney
        Mockito.`when`(context.getString(Matchers.eq(R.string.price_changed_from_TEMPLATE), Matchers.eq(originalPrice))).thenReturn(resultingMessage)
    }

    private fun givenResources(checkInOutDatesFormatted: String, city: String, numberNights: String, numberGuests: String) {
        Mockito.`when`(context.getString(Matchers.eq(R.string.calendar_instructions_date_range_TEMPLATE), Matchers.anyString(), Matchers.anyString())).thenReturn(checkInOutDatesFormatted)
        Mockito.`when`(resources.getString(Matchers.eq(R.string.single_line_street_address_TEMPLATE), Matchers.anyString(), Matchers.anyString())).thenReturn(city)
        Mockito.`when`(resources.getQuantityString(Matchers.eq(R.plurals.number_of_nights), Matchers.anyInt(), Matchers.anyInt())).thenReturn(numberNights)
        Mockito.`when`(resources.getQuantityString(Matchers.eq(R.plurals.number_of_guests), Matchers.anyInt(), Matchers.anyInt())).thenReturn(numberGuests)
    }

    private fun setup() {
        Mockito.`when`(context.resources).thenReturn(resources)
        sut = HotelCheckoutSummaryViewModel(context)
    }
}
