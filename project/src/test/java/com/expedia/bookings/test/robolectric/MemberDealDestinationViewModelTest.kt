package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.sos.MemberDealDestination
import com.expedia.bookings.mia.vm.MemberDealDestinationViewModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class MemberDealDestinationViewModelTest {

    lateinit var vm: MemberDealDestinationViewModel
    lateinit var hotel: MemberDealDestination.Hotel
    lateinit var context: Context

    @Before
    fun before() {
        hotel = MemberDealDestination().Hotel()
        hotel.destination = MemberDealDestination().Hotel().Destination()
        hotel.destination?.regionID = "800103"
        hotel.destination?.shortName = "Paris"
        hotel.offerDateRange = MemberDealDestination().Hotel().OfferDateRange()
        hotel.offerDateRange?.travelStartDate = arrayListOf(2017, 5, 8)
        hotel.offerDateRange?.travelEndDate = arrayListOf(2017, 5, 9)
        hotel.hotelPricingInfo = MemberDealDestination().Hotel().HotelPricingInfo()
        hotel.hotelPricingInfo?.percentSavings = 50.0
        hotel.hotelPricingInfo?.averagePriceValue = 130.06
        hotel.hotelPricingInfo?.crossOutPriceValue = 260.12
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMemberDealDestinationViewModel() {
        setupSystemUnderTest()
        assertEquals("https://a.travel-assets.com/dynamic_images/800103.jpg", vm.backgroundUrl)
        assertEquals("Paris", vm.cityName)
        assertEquals("Mon, May 8 - Tue, May 9", vm.dateRangeText)
        assertEquals("-50%", vm.percentSavingsText)
        assertEquals("$130", vm.priceText.toString())
        assertEquals("$260", vm.strikeOutPriceText.toString())
    }

    private fun setupSystemUnderTest() {
        context = RuntimeEnvironment.application
        setPOS(PointOfSaleId.UNITED_STATES)
        vm = MemberDealDestinationViewModel(context, hotel, "USD")
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
    }
}
