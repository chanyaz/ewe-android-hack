package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.hotel.widget.HotelSelectARoomBarViewModel
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelSelectARoomViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private val context = RuntimeEnvironment.application

    private lateinit var hotelOffersResponse: HotelOffersResponse

    @Test
    fun testNegativePrice() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()

        val hotelRate = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        hotelRate?.priceToShowUsers = -10f
        val viewModel = HotelSelectARoomBarViewModel(context)
        viewModel.response = hotelOffersResponse
        assertEquals("From $0", viewModel.getPriceString().toString())
    }

    @Test
    fun testNoStrikeThrough() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()

        val viewModel = HotelSelectARoomBarViewModel(context)
        viewModel.response = hotelOffersResponse

        assertEquals("", viewModel.getStrikeThroughPriceString())
        assertEquals("From $109", viewModel.getPriceString().toString())
        assertEquals("Select a Room From $109 button", viewModel.getContainerContentDescription())
    }

    @Test
    fun testShowStrikeThroughAirAttach() {
        hotelOffersResponse = mockHotelServiceTestRule.getAirAttachedHotelOffersResponse()

        val viewModel = HotelSelectARoomBarViewModel(context)
        viewModel.response = hotelOffersResponse

        assertEquals("$284", viewModel.getStrikeThroughPriceString().toString())
        assertEquals("From $241", viewModel.getPriceString().toString())
        assertEquals("Select a Room From $241 button", viewModel.getContainerContentDescription())
    }

    @Test
    fun testShowStrikeThroughSwp() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
        val chargeableRateInfo = hotelOffersResponse.hotelRoomResponse.firstOrNull()?.rateInfo?.chargeableRateInfo
        chargeableRateInfo?.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        chargeableRateInfo?.strikethroughPriceToShowUsers = 100f
        chargeableRateInfo?.priceToShowUsers = 99f

        val viewModel = HotelSelectARoomBarViewModel(context)
        viewModel.response = hotelOffersResponse

        assertEquals("$100", viewModel.getStrikeThroughPriceString().toString())
        assertEquals("From $99", viewModel.getPriceString().toString())
        assertEquals("Select a Room From $99 button", viewModel.getContainerContentDescription())
    }

    @Test
    fun testNullRoom() {
        val viewModel = HotelSelectARoomBarViewModel(context)
        assertEquals("", viewModel.getStrikeThroughPriceString())
        assertEquals("", viewModel.getPriceString().toString())
        assertEquals("Select a Room  button", viewModel.getContainerContentDescription())

        viewModel.response = HotelOffersResponse()
        assertEquals("", viewModel.getStrikeThroughPriceString())
        assertEquals("", viewModel.getPriceString().toString())
        assertEquals("Select a Room  button", viewModel.getContainerContentDescription())

        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
        hotelOffersResponse.hotelRoomResponse.firstOrNull()?.rateInfo?.chargeableRateInfo = null
        viewModel.response = hotelOffersResponse
        assertEquals("", viewModel.getStrikeThroughPriceString())
        assertEquals("", viewModel.getPriceString().toString())
        assertEquals("Select a Room  button", viewModel.getContainerContentDescription())
    }

    @Test fun testViewModelOutputsForViewWhenRoomOffersAreNotAvailable() {
        hotelOffersResponse = mockHotelServiceTestRule.getRoomOffersNotAvailableHotelOffersResponse()

        val viewModel = HotelSelectARoomBarViewModel(context)
        viewModel.response = hotelOffersResponse

        assertEquals("", viewModel.getStrikeThroughPriceString())
        assertEquals("", viewModel.getPriceString().toString())
        assertEquals("Select a Room  button", viewModel.getContainerContentDescription())
    }
}
