package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailAmenitiesViewModel
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailAmenitiesViewModelTest {
    lateinit var amenitiesVM: RailAmenitiesViewModel

    @Test
    fun outboundLegNotSet() {
        amenitiesVM = RailAmenitiesViewModel()
        amenitiesVM.offerObservable.onNext(RailOffer())

        assertTrue(amenitiesVM.segmentAmenitiesSubject.value.isEmpty())
    }

    @Test
    fun missingFareBreakdown() {
        amenitiesVM = RailAmenitiesViewModel()
        amenitiesVM.offerObservable.onNext(buildRailOfferWithNoFareBreakdown())

        assertSegmentFaresMissing(amenitiesVM.segmentAmenitiesSubject.value)
    }

    @Test
    fun segmentsHavePassengerFares() {
        amenitiesVM = RailAmenitiesViewModel()
        amenitiesVM.offerObservable.onNext(buildRailOfferWithFareBreakdowns())

        assertSegmentFaresPopulated(amenitiesVM.segmentAmenitiesSubject.value)
    }

    private fun assertSegmentFaresPopulated(pairs: List<Pair<RailSegment, PassengerSegmentFare?>>) {
        assertEquals(2, pairs.size)
        assertNotNull(pairs[0].second)
        assertNotNull(pairs[1].second)

        assertEquals(pairs[0].first.travelSegmentIndex, pairs[0].second?.travelSegmentIndex)
        assertEquals(pairs[1].first.travelSegmentIndex, pairs[1].second?.travelSegmentIndex)
    }

    private fun assertSegmentFaresMissing(pairs: List<Pair<RailSegment, PassengerSegmentFare?>>) {
        assertEquals(2, pairs.size)
        assertNull(pairs[0].second)
        assertNull(pairs[1].second)
    }

    private fun buildRailOfferWithFareBreakdowns(): RailOffer {
        var offer = buildRailOfferWithNoFareBreakdown()
        //quite an atrocity
        offer.railProductList[0].fareBreakdownList = ArrayList<RailProduct.FareBreakdown>()
        offer.railProductList[0].fareBreakdownList.add(RailProduct.FareBreakdown())
        offer.railProductList[0].fareBreakdownList[0].passengerFareList = ArrayList<RailProduct.PassengerFare>()
        offer.railProductList[0].fareBreakdownList[0].passengerFareList.add(RailProduct.PassengerFare())
        offer.railProductList[0].fareBreakdownList[0].passengerFareList[0].passengerSegmentFareList = ArrayList<PassengerSegmentFare>()

        var segmentFare1 = PassengerSegmentFare()
        segmentFare1.travelSegmentIndex = 1;
        var segmentFare2 = PassengerSegmentFare()
        segmentFare2.travelSegmentIndex = 2;
        offer.railProductList[0].fareBreakdownList[0].passengerFareList[0].passengerSegmentFareList.add(segmentFare1)
        offer.railProductList[0].fareBreakdownList[0].passengerFareList[0].passengerSegmentFareList.add(segmentFare2)
        return offer
    }

    private fun buildRailOfferWithNoFareBreakdown(): RailOffer {
        var legOption = buildLegOptionWithSegments()
        var offer = RailOffer()
        offer.outboundLeg = legOption

        offer.railProductList = ArrayList<RailProduct>()
        offer.railProductList.add(RailProduct())
        return offer
    }

    private fun buildLegOptionWithSegments(): RailLegOption {
        var legOption = RailLegOption()
        legOption.travelSegmentList = ArrayList<RailSegment>()
        var segment1 = RailSegment()
        segment1.travelSegmentIndex = 1
        var segment2 = RailSegment()
        segment2.travelSegmentIndex = 2

        legOption.travelSegmentList.add(segment1)
        legOption.travelSegmentList.add(segment2)

        return legOption
    }
}
