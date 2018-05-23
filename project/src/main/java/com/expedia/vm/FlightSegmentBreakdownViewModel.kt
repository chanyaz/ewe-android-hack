package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightTripDetails
import io.reactivex.subjects.PublishSubject

class FlightSegmentBreakdownViewModel(val context: Context) {
    val addSegmentRowsObserver = PublishSubject.create<List<FlightSegmentBreakdown>>()
    val updateSeatClassAndCodeSubject = PublishSubject.create<List<FlightTripDetails.SeatClassAndBookingCode>>()
    val isFareFamilyUpgraded = PublishSubject.create<Boolean>()
    val richContentFareFamilyDividerViewStream = PublishSubject.create<Boolean>()
    val richContentFareFamilyWifiViewStream = PublishSubject.create<Boolean>()
    val richContentFareFamilyEntertainmentViewStream = PublishSubject.create<Boolean>()
    val richContentFareFamilyPowerViewStream = PublishSubject.create<Boolean>()

    init {
        isFareFamilyUpgraded.filter { it }.subscribe {
            richContentFareFamilyWifiViewStream.onNext(false)
            richContentFareFamilyEntertainmentViewStream.onNext(false)
            richContentFareFamilyPowerViewStream.onNext(false)
            richContentFareFamilyDividerViewStream.onNext(false)
        }
    }
}
