package com.expedia.bookings.itin.flight.details

import com.expedia.bookings.data.trips.ItinCardDataFlight
import io.reactivex.subjects.BehaviorSubject

class FlightItinMapWidgetViewModel {
    val itinCardDataObservable = BehaviorSubject.create<ItinCardDataFlight>()
}
