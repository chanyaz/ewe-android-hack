package com.expedia.bookings.itin.triplist

import com.expedia.bookings.tracking.ITripsTracking
import io.reactivex.subjects.PublishSubject

class TripListFragmentViewModel(val tripsTracking: ITripsTracking) : ITripListFragmentViewModel {

    override val tripListVisitTrackingSubject: PublishSubject<Int> = PublishSubject.create()
    override val tabSelectedSubject: PublishSubject<Int> = PublishSubject.create()

    init {
        tripListVisitTrackingSubject.subscribe { tabPosition ->
            tripsTracking.trackTripListVisit(tabPosition)
        }

        tabSelectedSubject.subscribe {
            //TODO actions on selecting a tab
        }
    }
}
