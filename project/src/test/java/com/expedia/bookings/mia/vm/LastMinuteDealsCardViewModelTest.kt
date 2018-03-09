package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class LastMinuteDealsCardViewModelTest {

    lateinit var vm: BaseDealsCardViewModel
    lateinit var hotel: DealsDestination.Hotel
    val context: Context = RuntimeEnvironment.application

    @Before
    fun before() {
        hotel = DealsDestination().Hotel()
        hotel.destination = DealsDestination().Hotel().Destination()
        hotel.destination?.regionID = "800103"
        hotel.destination?.shortName = "Paris"
        hotel.offerDateRange = DealsDestination().Hotel().OfferDateRange()
        hotel.offerDateRange?.travelStartDate = arrayListOf(2017, 5, 8)
        hotel.offerDateRange?.travelEndDate = arrayListOf(2017, 5, 9)
        hotel.hotelPricingInfo = DealsDestination().Hotel().HotelPricingInfo()
        hotel.hotelInfo = DealsDestination().Hotel().HotelInfo()
        hotel.hotelInfo?.hotelId = "12345"
        hotel.hotelInfo?.hotelName = "non loc hotel name"
        hotel.hotelInfo?.localizedHotelName = "loc hotel name"
        hotel.hotelPricingInfo?.percentSavings = 50.0
        hotel.hotelPricingInfo?.averagePriceValue = 130.06
        hotel.hotelPricingInfo?.crossOutPriceValue = 260.12
    }

    @Test
    fun numberOfTravelers_isTwo() {
        setupSystemUnderTest()
        assertEquals(2, vm.numberOfTravelers)
    }

    @Test
    fun title_isNonLocalizedHotelName_givenLocalizedHotelNameUnavailable() {
        hotel.hotelInfo?.localizedHotelName = ""
        setupSystemUnderTest()
        assertEquals("non loc hotel name", vm.title)
    }

    @Test
    fun title_isLocalizedHotelName_byDefault() {
        setupSystemUnderTest()
        kotlin.test.assertEquals("loc hotel name", vm.title)
    }

    @Test
    fun subtitle_isCity() {
        setupSystemUnderTest()
        assertEquals("Paris", vm.subtitle)
    }

    @Test
    fun hotelId_isSet() {
        setupSystemUnderTest()
        assertEquals("12345", vm.hotelId)
    }

    @Test
    fun hotelBackgroundImageURL_isFormattedWithHighResImage() {
        hotel.hotelInfo?.hotelImageUrl = "https://images.trvl-media.com/hotels/2000000/1450000/1445800/1445791/3c7df4c6_l.jpg"
        setupSystemUnderTest()
        assertEquals(2, vm.prioritizedBackgroundImageUrls.size)
        assertEquals("https://images.trvl-media.com/hotels/2000000/1450000/1445800/1445791/3c7df4c6_z.jpg", vm.prioritizedBackgroundImageUrls[0])
    }

    @Test
    fun hotelBackgroundImageURL_appendsHighResString_givenUrlWithoutUnderscorePattern() {
        hotel.hotelInfo?.hotelImageUrl = "https://images.trvl-media.com/hotels/2000000/1450000/1445800/1445791/3c7df4c6.jpg"
        setupSystemUnderTest()
        assertEquals(2, vm.prioritizedBackgroundImageUrls.size)
        assertEquals("https://images.trvl-media.com/hotels/2000000/1450000/1445800/1445791/3c7df4c6_z.jpg", vm.prioritizedBackgroundImageUrls[0])
    }

    @Test
    fun discountColors_areLastMinuteDealsColors() {
        setupSystemUnderTest()
        assertEquals(DiscountColors.LAST_MINUTE_DEALS, vm.discountColors)
    }

    @Test
    fun cardContentDescription_isCorrect() {
        hotel.hotelInfo?.localizedHotelName = "Fancy Resort"
        setupSystemUnderTest()
        assertEquals("Fancy Resort. Mon May 08, 2017 to Tue May 09, 2017 Original price discounted 50%. Regularly \$260, now \$130. ", vm.getCardContentDescription())
    }

    private fun setupSystemUnderTest() {
        vm = LastMinuteDealsCardViewModel(context, hotel, "USD")
    }
}
