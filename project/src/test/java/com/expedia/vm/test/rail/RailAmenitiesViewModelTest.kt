package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailAmenitiesViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class RailAmenitiesViewModelTest {
    lateinit var amenitiesVM: RailAmenitiesViewModel
    lateinit var testLegOption: RailLegOption
    lateinit var testRailProduct: RailProduct

    @Before
    fun setUp() {
        testLegOption = generateLegOption()
        testRailProduct = generateRailProduct()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun outboundLegNotSet() {
        amenitiesVM = RailAmenitiesViewModel()
        amenitiesVM.railProductObservable.onNext(testRailProduct)

        assertNull(amenitiesVM.segmentAmenitiesSubject.value)
    }

    @Test
    fun availableSegmentFares() {
        amenitiesVM = RailAmenitiesViewModel()
        amenitiesVM.legOptionObservable.onNext(testLegOption)
        amenitiesVM.railProductObservable.onNext(testRailProduct)

        assertSegmentFaresPopulated(amenitiesVM.segmentAmenitiesSubject.value)
    }

    private fun assertSegmentFaresPopulated(pairs: List<Pair<RailSegment, PassengerSegmentFare?>>) {
        assertEquals(3, pairs.size)
        assertNotNull(pairs[0].second)
        assertNotNull(pairs[1].second)

        assertEquals(pairs[0].first.travelSegmentIndex, pairs[0].second?.travelSegmentIndex)
        assertEquals(pairs[1].first.travelSegmentIndex, pairs[1].second?.travelSegmentIndex)
    }

    private fun generateRailProduct(): RailProduct {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_product_segments_8_9_10.json")
        val railProduct = resourceReader.constructUsingGson(RailProduct::class.java)
        return railProduct
    }

    private fun generateLegOption(): RailLegOption {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_leg_option_segments_8_9_10.json")
        val legOption = resourceReader.constructUsingGson(RailLegOption::class.java)
        return legOption
    }
}
