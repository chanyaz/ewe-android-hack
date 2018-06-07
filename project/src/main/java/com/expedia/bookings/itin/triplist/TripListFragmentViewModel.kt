package com.expedia.bookings.itin.triplist

import com.expedia.bookings.tracking.ITripsTracking
import io.reactivex.subjects.PublishSubject

class TripListFragmentViewModel(val tripsTracking: ITripsTracking) : ITripListFragmentViewModel {

    override val tripListVisitTrackingSubject: PublishSubject<Int> = PublishSubject.create()
    override val tabSelectedSubject: PublishSubject<Int> = PublishSubject.create()

    init {
        tripListVisitTrackingSubject.subscribe { tabPosition ->
            when (tabPosition) {
                TripListTabs.UPCOMING_TAB.value -> tripsTracking.trackTripListUpcomingTabVisit()
                TripListTabs.PAST_TAB.value -> tripsTracking.trackTripListPastTabVisit()
                TripListTabs.CANCELLED_TAB.value -> tripsTracking.trackTripListCancelledTabVisit()
            }
        }

        tabSelectedSubject.subscribe {
            //TODO actions on selecting a tab
        }
    }
}
