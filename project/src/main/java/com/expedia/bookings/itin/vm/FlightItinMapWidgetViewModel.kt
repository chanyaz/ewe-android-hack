package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.trips.ItinCardDataFlight
import io.reactivex.subjects.BehaviorSubject

class FlightItinMapWidgetViewModel {
    val itinCardDataObservable = BehaviorSubject.create<ItinCardDataFlight>()
}
