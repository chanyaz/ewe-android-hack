package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.CollectionUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class RailSegmentAmenitiesViewModel(val context: Context) {
    val segmentAmenitiesObservable = PublishSubject.create<Pair<RailSegment, PassengerSegmentFare?>>()

    //outputs
    val noAmenitiesObservable = PublishSubject.create<Boolean>()
    val stationDescriptionObservable = PublishSubject.create<String>()
    val fareInfoObservable = PublishSubject.create<String>()
    val formattedAmenitiesObservable = PublishSubject.create<String>()

    init {
        segmentAmenitiesObservable.subscribe { pair ->
            val segment = pair.first

            val formattedStationInfo = Phrase.from(context, R.string.rail_station_info_TEMPLATE)
                    .put("departure", segment.departureStation.stationDisplayName)
                    .put("arrival", segment.arrivalStation.stationDisplayName)
                    .format().toString()

            stationDescriptionObservable.onNext(formattedStationInfo)

            val segmentFare = pair.second
            fareInfoObservable.onNext(formatFareInfo(segmentFare))
            if (segmentFare == null || CollectionUtils.isEmpty(segmentFare.amenityList)) {
                noAmenitiesObservable.onNext(true)
            } else {
                noAmenitiesObservable.onNext(false)
                formattedAmenitiesObservable.onNext(formatAmenitiesString(segmentFare.amenityList))
            }
        }
    }

    private fun formatFareInfo(segmentFare: PassengerSegmentFare?): String {
        val formattedFareInfo = Phrase.from(context, R.string.rail_fare_info_TEMPLATE)
                .put("serviceclass", segmentFare?.carrierServiceClassDisplayName ?: "")
                .put("fareclass", segmentFare?.carrierFareClassDisplayName ?: "")
                .format().toString()
        return formattedFareInfo
    }

    private fun formatAmenitiesString(amenityList: List<PassengerSegmentFare.Amenity>): String {
        val sb = StringBuffer()
        for (amenity in amenityList) {
            sb.append(context.getString(R.string.bullet_point))
                    .append(" ")
                    .append(amenity.displayName)
                    .append("<br/>")
        }
        return sb.toString()
    }
}

