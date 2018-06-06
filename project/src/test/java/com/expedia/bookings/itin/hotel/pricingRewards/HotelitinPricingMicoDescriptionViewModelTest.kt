package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.data.ItinCruise
import com.expedia.bookings.itin.tripstore.data.ItinFlight
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.data.ItinRail
import com.expedia.bookings.itin.tripstore.extensions.HasProducts
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class HotelitinPricingMicoDescriptionViewModelTest {

    private val mockItinPackageHotel = ItinMocker.hotelPackageHappy
    private val mockItinMickoHotel = ItinMocker.mickoHotelHappy

    private lateinit var micoContainerResetObserver: TestObserver<Unit>
    private lateinit var micoProductDescriptionObserver: TestObserver<HotelItinMicoItem>

    @Before
    fun setUp() {
        micoContainerResetObserver = TestObserver()
        micoProductDescriptionObserver = TestObserver()
    }

    @After
    fun tearDown() {
        micoContainerResetObserver.dispose()
        micoProductDescriptionObserver.dispose()
    }

    @Test
    fun testBundleContentsLabelPackage() {
        val viewModel = getViewModel()
        micoContainerResetObserver.assertEmpty()
        micoProductDescriptionObserver.assertEmpty()

        viewModel.itinObserver.onChanged(mockItinPackageHotel)

        micoContainerResetObserver.assertValueCount(1)
        micoProductDescriptionObserver.assertValueCount(3)
        val bundleItem = micoProductDescriptionObserver.values()[0]
        val expectedString = (R.string.itin_hotel_details_price_summary_mico_description).toString()
        assertEquals(expectedString, bundleItem.labelString)

        val bundleItem2 = micoProductDescriptionObserver.values()[1]
        val expectedProductString = (R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        assertEquals(expectedProductString, bundleItem2.labelString)
    }

    @Test
    fun testBundleContentsLabelMico() {
        val viewModel = getViewModel()
        micoContainerResetObserver.assertEmpty()
        micoProductDescriptionObserver.assertEmpty()

        viewModel.itinObserver.onChanged(mockItinMickoHotel)

        micoContainerResetObserver.assertValueCount(1)
        micoProductDescriptionObserver.assertValueCount(3)
        val bundleItem = micoProductDescriptionObserver.values()[0]
        val expectedString = (R.string.itin_hotel_details_price_summary_mico_description).toString()
        assertEquals(expectedString, bundleItem.labelString)

        val bundleItem2 = micoProductDescriptionObserver.values()[1]
        val expectedProductString = (R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        assertEquals(expectedProductString, bundleItem2.labelString)
    }

    @Test
    fun testProductsDescriptionString() {
        val viewModel = getViewModel()
        val mockProductContainer = MockProductContainer(null, null, null, null, null, null)

        mockProductContainer.mockList = listOf(TripProducts.HOTEL)
        var result = viewModel.getProductsDescriptionString(mockProductContainer)
        var expectedString = (R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        assertEquals(expectedString, result[0])

        mockProductContainer.mockList = listOf(TripProducts.FLIGHT, TripProducts.RAIL)
        result = viewModel.getProductsDescriptionString(mockProductContainer)
        val expectedStringFlight = (R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Flight).toString()))
        val expectedStringRail = (R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Rail).toString()))
        assertEquals(expectedStringFlight, result[0])
        assertEquals(expectedStringRail, result[1])
    }

    private fun getViewModel(): HotelItinPricingMicoDescriptionViewModel<MockHotelItinPricingMicoScope> {
        val viewModel = HotelItinPricingMicoDescriptionViewModel(MockHotelItinPricingMicoScope())

        viewModel.micoContainerResetSubject.subscribe(micoContainerResetObserver)
        viewModel.micoProductDescriptionSubject.subscribe(micoProductDescriptionObserver)

        return viewModel
    }

    class MockHotelItinPricingMicoScope : HasHotelRepo, HasStringProvider, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
    data class MockProductContainer(
            override val hotels: List<ItinHotel>?,
            override val flights: List<ItinFlight>?,
            override val activities: List<ItinLx>?,
            override val cars: List<ItinCar>?,
            override val cruises: List<ItinCruise>?,
            override val rails: List<ItinRail>?) : HasProducts {
            var mockList = listOf<TripProducts>()
            override fun listOfTripProducts(): List<TripProducts> {
            return mockList
        }
    }
