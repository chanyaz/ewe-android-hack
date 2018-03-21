package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightTripDetails
import io.reactivex.subjects.PublishSubject

class FlightSegmentBreakdownViewModel(val context: Context) {
    val addSegmentRowsObserver = PublishSubject.create<List<FlightSegmentBreakdown>>()
    val updateSeatClassAndCodeSubject = PublishSubject.create<List<FlightTripDetails.SeatClassAndBookingCode>>()
}
