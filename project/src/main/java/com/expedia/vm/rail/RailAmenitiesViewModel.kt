package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSegment
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class RailAmenitiesViewModel {
    val legOptionObservable = BehaviorSubject.create<RailLegOption>()
    val railProductObservable = BehaviorSubject.create<RailProduct>()

    //outputs
    val segmentAmenitiesSubject = BehaviorSubject.create<List<Pair<RailSegment, PassengerSegmentFare>>>()

    init {
        Observable.combineLatest(legOptionObservable, railProductObservable,
                { legOption, product ->
                    getAmenitiesForSegments(legOption, product)
                }).subscribe(segmentAmenitiesSubject)

    }

    private fun getAmenitiesForSegments(legOption: RailLegOption, railProduct: RailProduct): List<Pair<RailSegment, PassengerSegmentFare>> {

        var amenities = ArrayList<Pair<RailSegment, PassengerSegmentFare>>()

        val segmentFareDetails = railProduct.segmentFareDetailList
        val travelSegments = legOption.travelSegmentList

        for (segmentFareDetail in segmentFareDetails) {
            val travelSegmentIndex = segmentFareDetail.travelSegmentIndex
            for (travelSegment in travelSegments) {
                if (travelSegment.travelSegmentIndex == travelSegmentIndex) {
                    amenities.add(Pair(travelSegment, segmentFareDetail))
                    break
                }
            }
        }
        return amenities
    }
}