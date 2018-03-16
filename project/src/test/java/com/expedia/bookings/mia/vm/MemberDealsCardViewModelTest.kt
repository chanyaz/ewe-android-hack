package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class MemberDealsCardViewModelTest {

    lateinit var vm: BaseDealsCardViewModel
    lateinit var hotel: DealsDestination.Hotel
    val context: Context = RuntimeEnvironment.application

    @Before
    fun before() {
        hotel = DealsDestination.Hotel()
        hotel.destination = DealsDestination.Hotel.Destination()
        hotel.destination?.regionID = "800103"
        hotel.destination?.shortName = "Paris"
        hotel.offerDateRange = DealsDestination.Hotel.OfferDateRange()
        hotel.offerDateRange?.travelStartDate = arrayListOf(2017, 5, 8)
        hotel.offerDateRange?.travelEndDate = arrayListOf(2017, 5, 9)
        hotel.hotelPricingInfo = DealsDestination.Hotel.HotelPricingInfo()
        hotel.hotelInfo = DealsDestination.Hotel.HotelInfo()
        hotel.hotelInfo?.hotelId = "12345"
        hotel.hotelInfo?.hotelName = "fancy hotel"
        hotel.hotelPricingInfo?.percentSavings = 50.0
        hotel.hotelPricingInfo?.averagePriceValue = 130.06
        hotel.hotelPricingInfo?.crossOutPriceValue = 260.12
    }

    @Test
    fun numberOfTravelers_isOne() {
        setupSystemUnderTest()
        assertEquals(1, vm.numberOfTravelers)
    }

    @Test
    fun title_isCity() {
        setupSystemUnderTest()
        assertEquals("Paris", vm.title)
    }

    @Test
    fun subtitle_isHotelOnly() {
        setupSystemUnderTest()
        assertEquals("Hotel Only", vm.subtitle)
    }

    @Test
    fun hotelId_isNull() {
        setupSystemUnderTest()
        assertNull(vm.hotelId)
    }

    @Test
    fun backgroundUrlList_isValid() {
        setupSystemUnderTest()
        assertEquals(1, vm.prioritizedBackgroundImageUrls.size)
        assertEquals("https://a.travel-assets.com/dynamic_images/800103.jpg", vm.prioritizedBackgroundImageUrls[0])
    }

    @Test
    fun backgroundUrl_isNull_givenNullRegionId() {
        hotel.destination?.regionID = null
        setupSystemUnderTest()
        assertEquals(1, vm.prioritizedBackgroundImageUrls.size)
        assertNull(vm.prioritizedBackgroundImageUrls[0])
    }

    @Test
    fun discountColors_areDefault_byDefault() {
        setupSystemUnderTest()
        assertEquals(DiscountColors.DEFAULT, vm.discountColors)
    }

    @Test
    fun discountColors_areMemberDealsColors_whenBucketed() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppBrandColors)
        setupSystemUnderTest()
        assertEquals(DiscountColors.MEMBER_DEALS, vm.discountColors)
    }

    @Test
    fun cardContentDescription_isCorrect() {
        setupSystemUnderTest()
        assertEquals("Paris. Mon May 08, 2017 to Tue May 09, 2017 Original price discounted 50%. Regularly \$260, now \$130. Hotel Only", vm.getCardContentDescription())
    }

    private fun setupSystemUnderTest() {
        vm = MemberDealsCardViewModel(context, hotel, "USD")
    }
}
