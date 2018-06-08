package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class HotelitinPricingBundleDescriptionViewModelTest {

    private val mockItinPackageHotel = ItinMocker.hotelPackageHappy
    private val mockItinMickoHotel = ItinMocker.mickoHotelHappy
    private val mockItinStandAloneHotel = ItinMocker.hotelDetailsHappy
    private val mockItinMultiHotel = ItinMocker.mickoMultiHotel
    private val mockMultiFlightOneWay = ItinMocker.midMultipleFlightsTripDetails
    private val mockMultiDestinationFlight = ItinMocker.itinMickoMultiDestinationFlight

    private lateinit var bundleContainerResetObserver: TestObserver<Unit>
    private lateinit var bundleProductDescriptionObserver: TestObserver<String>
    private lateinit var bundleContainerViewVisibilityObserver: TestObserver<Boolean>

    @Before
    fun setUp() {
        bundleContainerResetObserver = TestObserver()
        bundleProductDescriptionObserver = TestObserver()
        bundleContainerViewVisibilityObserver = TestObserver()
    }

    @After
    fun tearDown() {
        bundleContainerResetObserver.dispose()
        bundleProductDescriptionObserver.dispose()
        bundleContainerViewVisibilityObserver.dispose()
    }

    @Test
    fun testBundleContainerVisibilitySubject() {
        val viewModel = getViewModel()
        viewModel.bundleContainerViewVisibilitySubject.subscribe(bundleContainerViewVisibilityObserver)
        bundleContainerViewVisibilityObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockItinStandAloneHotel)
        bundleContainerViewVisibilityObserver.assertValueCount(1)
        bundleContainerViewVisibilityObserver.assertValuesAndClear(false)
        viewModel.itinObserver.onChanged(mockItinMickoHotel)
        bundleContainerViewVisibilityObserver.assertValueCount(1)
        bundleContainerViewVisibilityObserver.assertValuesAndClear(true)
        viewModel.itinObserver.onChanged(mockItinPackageHotel)
        bundleContainerViewVisibilityObserver.assertValueCount(1)
        bundleContainerViewVisibilityObserver.assertValuesAndClear(true)
    }

    @Test
    fun testBundleContainerResetSubject() {
        val viewModel = getViewModel()
        viewModel.bundleContainerResetSubject.subscribe(bundleContainerResetObserver)
        bundleContainerResetObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockItinStandAloneHotel)
        bundleContainerResetObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockItinMickoHotel)
        bundleContainerResetObserver.assertValueCount(1)
        bundleContainerResetObserver.assertValuesAndClear(Unit)
        viewModel.itinObserver.onChanged(mockItinPackageHotel)
        bundleContainerResetObserver.assertValueCount(1)
        bundleContainerResetObserver.assertValuesAndClear(Unit)
    }

    @Test
    fun testProductDescriptionStandAloneHotel() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockItinStandAloneHotel)
        bundleProductDescriptionObserver.assertNoValues()
    }

    @Test
    fun testProductDescriptionPackage() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockItinPackageHotel)
        val expectedString = (R.string.itin_hotel_details_price_summary_bundle_description).toString()
        val roundTripFlight = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.itin_flight_type_roundtrip).toString()))
        val hotel = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))

        bundleProductDescriptionObserver.assertValueCount(3)
        assertEquals(expectedString, bundleProductDescriptionObserver.values()[0])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[1])
        assertEquals(roundTripFlight, bundleProductDescriptionObserver.values()[2])
    }

    @Test
    fun testProductDescriptionMicko() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockItinPackageHotel)
        val expectedString = (R.string.itin_hotel_details_price_summary_bundle_description).toString()
        val hotel = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        val roundTripFlight = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.itin_flight_type_roundtrip).toString()))

        bundleProductDescriptionObserver.assertValueCount(3)
        assertEquals(expectedString, bundleProductDescriptionObserver.values()[0])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[1])
        assertEquals(roundTripFlight, bundleProductDescriptionObserver.values()[2])
    }

    @Test
    fun testProductDescriptionMulitHotel() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockItinMultiHotel)
        val expectedString = (R.string.itin_hotel_details_price_summary_bundle_description).toString()
        val hotel = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))

        bundleProductDescriptionObserver.assertValueCount(3)
        assertEquals(expectedString, bundleProductDescriptionObserver.values()[0])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[1])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[2])
    }

    @Test
    fun testProductDescriptionOneWayFlight() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockMultiFlightOneWay)
        val expectedString = (R.string.itin_hotel_details_price_summary_bundle_description).toString()
        val hotel = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        val oneWayTripFlight = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.itin_flight_type_one_way).toString()))

        bundleProductDescriptionObserver.assertValueCount(4)
        assertEquals(expectedString, bundleProductDescriptionObserver.values()[0])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[1])
        assertEquals(oneWayTripFlight, bundleProductDescriptionObserver.values()[2])
        assertEquals(oneWayTripFlight, bundleProductDescriptionObserver.values()[3])

    }

    @Test
    fun testProductDescriptionMultiDestinationFlight() {
        val viewModel = getViewModel()
        viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertNoValues()
        viewModel.itinObserver.onChanged(mockMultiDestinationFlight)
        val expectedString = (R.string.itin_hotel_details_price_summary_bundle_description).toString()
        val hotel = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.Hotel).toString()))
        val multiDestinationTripFlight = (R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE).toString()
                .plus(mapOf("product" to (R.string.itin_flight_type_multi_destination).toString()))

        bundleProductDescriptionObserver.assertValueCount(3)
        assertEquals(expectedString, bundleProductDescriptionObserver.values()[0])
        assertEquals(hotel, bundleProductDescriptionObserver.values()[1])
        assertEquals(multiDestinationTripFlight, bundleProductDescriptionObserver.values()[2])
    }

    private fun getViewModel(): HotelItinPricingBundleDescriptionViewModel<MockHotelItinPricingBundleScope> {
        return HotelItinPricingBundleDescriptionViewModel(MockHotelItinPricingBundleScope())
    }

    class MockHotelItinPricingBundleScope : HasHotelRepo, HasStringProvider, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
