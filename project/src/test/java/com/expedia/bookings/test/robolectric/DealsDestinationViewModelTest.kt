package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DealsDestinationViewModelTest {

    lateinit var vm: DealsDestinationViewModel
    lateinit var hotel: DealsDestination.Hotel
    lateinit var context: Context

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
        hotel.hotelPricingInfo?.percentSavings = 50.0
        hotel.hotelPricingInfo?.averagePriceValue = 130.06
        hotel.hotelPricingInfo?.crossOutPriceValue = 260.12
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMemberDealDestinationViewModel() {
        setupSystemUnderTest()
        assertEquals("https://a.travel-assets.com/dynamic_images/800103.jpg", vm.backgroundUrl)
        assertEquals("Paris", vm.cityName)
        assertEquals("Mon, May 8 - Tue, May 9", vm.dateRangeText)
        assertEquals("-50%", vm.percentSavingsText)
        assertEquals("$130", vm.priceText.toString())
        assertEquals("$260", vm.strikeOutPriceText.toString())
    }

    @Test
    fun getDiscountPercentForContentDesc_calculatesDiscountWhenPercentSavingsIsNull() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.totalPriceValue = 150.06
        assertEquals("42%", vm.getDiscountPercentForContentDesc(null))
    }

    @Test
    fun getDiscountPercentForContentDesc_returnsEmptyStringWhenCrossOutPriceIsNull() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.crossOutPriceValue = null
        hotel.hotelPricingInfo?.totalPriceValue = 150.06
        assertEquals("", vm.getDiscountPercentForContentDesc(null))
    }

    @Test
    fun getDiscountPercentForContentDesc_returnsEmptyStringWhenTotalPriceIsNull() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.totalPriceValue = null
        assertEquals("", vm.getDiscountPercentForContentDesc(null))
    }

    @Test
    fun getPercentSavingsText_returnsSavingsCalculatedFromCrossOutPriceAndTotalPriceValue_givenPercentSavingsIsNull() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.totalPriceValue = 150.06
        assertEquals("-42%", vm.getPercentSavingsText(null))
    }

    @Test
    fun getPercentSavingsText_returnsFormattedString() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.totalPriceValue = 150.06
        assertEquals("-50%", vm.getPercentSavingsText(hotel.hotelPricingInfo?.percentSavings))
    }

    private fun setupSystemUnderTest() {
        context = RuntimeEnvironment.application
        setPOS(PointOfSaleId.UNITED_STATES)
        vm = DealsDestinationViewModel(context, hotel, "USD")
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
    }
}
