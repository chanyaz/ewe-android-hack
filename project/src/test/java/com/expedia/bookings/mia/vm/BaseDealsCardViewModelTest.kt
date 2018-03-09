package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaseDealsCardViewModelTest {

    lateinit var vm: BaseDealsCardViewModel
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
        hotel.hotelInfo = DealsDestination().Hotel().HotelInfo()
        hotel.hotelPricingInfo?.percentSavings = 50.0
        hotel.hotelPricingInfo?.averagePriceValue = 130.06
        hotel.hotelPricingInfo?.crossOutPriceValue = 260.12
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMemberDealDestinationViewModel() {
        setupSystemUnderTest()
        assertEquals("Paris", vm.cityName)
        assertEquals("Mon, May 8 - Tue, May 9", vm.dateRangeText)
        assertEquals("-50%", vm.percentSavingsText)
        assertEquals("50%", vm.discountPercent)
        assertEquals("$130", vm.priceText.toString())
        assertEquals("$260", vm.strikeOutPriceText.toString())
    }

    @Test
    fun percentSavingsText_isEmpty_givenZeroSavings() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.percentSavings = 0.0
        assertEquals("", vm.percentSavingsText)
    }

    @Test
    fun percentSavingsText_isEmpty_givenNullSavings() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.percentSavings = null
        assertEquals("", vm.percentSavingsText)
    }

    @Test
    fun discountPercent_isEmpty_givenZeroSavings() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.percentSavings = 0.0
        assertEquals("", vm.discountPercent)
    }

    @Test
    fun discountPercent_isEmpty_givenNullSavings() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.percentSavings = null
        assertEquals("", vm.discountPercent)
    }

    @Test
    fun strikeOutPriceText_isHidden_givenCrossOutPriceValueOfZero() {
        setupSystemUnderTest()
        hotel.hotelPricingInfo?.crossOutPriceValue = 0.00
        assertEquals("", vm.strikeOutPriceText.toString())
    }

    private fun setupSystemUnderTest() {
        context = RuntimeEnvironment.application
        setPOS(PointOfSaleId.UNITED_STATES)
        vm = LastMinuteDealsCardViewModel(context, hotel, "USD")
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
    }
}
