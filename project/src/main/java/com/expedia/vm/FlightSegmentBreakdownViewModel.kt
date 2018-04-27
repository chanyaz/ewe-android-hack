package com.expedia.vm

import android.content.Context
import android.view.View
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.extensions.ObservableOld
import io.reactivex.subjects.PublishSubject

class FlightSegmentBreakdownViewModel(val context: Context) {
    val addSegmentRowsObserver = PublishSubject.create<List<FlightSegmentBreakdown>>()
    val updateSeatClassAndCodeSubject = PublishSubject.create<List<FlightTripDetails.SeatClassAndBookingCode>>()
    val segmentAmenitiesStream = PublishSubject.create<RichContent.RichContentAmenity>()
    val seatClassAndBookingCodeVisibilityStream = PublishSubject.create<Int>()
    val richContentDividerViewStream = PublishSubject.create<Boolean>()
    val richContentAmenitiesVisibilityStream = PublishSubject.create<Boolean>()
    val richContentWifiViewStream = PublishSubject.create<Boolean>()
    val richContentEntertainmentViewStream = PublishSubject.create<Boolean>()
    val richContentPowerViewStream = PublishSubject.create<Boolean>()

    init {
        segmentAmenitiesStream.subscribe { flightAmenities ->
            richContentWifiViewStream.onNext(flightAmenities.wifi)
            richContentEntertainmentViewStream.onNext(flightAmenities.entertainment)
            richContentPowerViewStream.onNext(flightAmenities.power)
            richContentAmenitiesVisibilityStream.onNext(flightAmenities.wifi || flightAmenities.entertainment || flightAmenities.power)
        }
        ObservableOld.combineLatest(seatClassAndBookingCodeVisibilityStream, richContentAmenitiesVisibilityStream, { seatClassAndBookingCodeVisibility, richContentAmenitiesVisibility ->
            (seatClassAndBookingCodeVisibility == View.VISIBLE && richContentAmenitiesVisibility)
        }).subscribe(richContentDividerViewStream)
    }
}
