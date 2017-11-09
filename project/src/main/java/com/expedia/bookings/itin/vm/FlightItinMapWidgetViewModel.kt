package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.trips.ItinCardDataFlight
import rx.subjects.BehaviorSubject

class FlightItinMapWidgetViewModel {
    val itinCardDataObservable = BehaviorSubject.create<ItinCardDataFlight>()
}
