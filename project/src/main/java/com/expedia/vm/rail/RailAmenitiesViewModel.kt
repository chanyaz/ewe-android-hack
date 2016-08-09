package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSegment
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import java.util.HashMap

class RailAmenitiesViewModel {
    val offerObservable = BehaviorSubject.create<RailOffer>()

    //outputs
    val segmentAmenitiesSubject = BehaviorSubject.create<List<Pair<RailSegment, PassengerSegmentFare?>>>()

    init {
        offerObservable.subscribe { offer ->
            val segmentAmenities = getAmenitiesForSegments(offer)
            segmentAmenitiesSubject.onNext(segmentAmenities)
        }
    }

    private fun getAmenitiesForSegments(offer: RailOffer): List<Pair<RailSegment, PassengerSegmentFare?>> {
        var amenities = ArrayList<Pair<RailSegment, PassengerSegmentFare?>>()

        if (offer.outboundLeg != null) {
            val segments = offer.outboundLeg?.travelSegmentList

            var segmentMapping = offer.railProductList?.firstOrNull()?.fareBreakdownList?.firstOrNull()?.passengerFareList?.firstOrNull()?.segmentToFareMapping
            if (segmentMapping == null) {
                segmentMapping = HashMap()
            }

            for (segment in segments!!) {
                amenities.add(Pair(segment, segmentMapping[segment.travelSegmentIndex]))
            }
        }
        return amenities;
    }
}