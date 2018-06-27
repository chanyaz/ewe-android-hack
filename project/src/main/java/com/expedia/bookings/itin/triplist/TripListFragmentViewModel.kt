package com.expedia.bookings.itin.triplist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.tracking.ITripsTracking
import io.reactivex.subjects.PublishSubject

class TripListFragmentViewModel(val tripsTracking: ITripsTracking, private val repository: ITripListRepository) : ITripListFragmentViewModel, ViewModel() {

    override val upcomingFoldersLiveData: MutableLiveData<List<TripFolder>> = MutableLiveData()
    override val tripListVisitTrackingSubject: PublishSubject<Int> = PublishSubject.create()
    override val tabSelectedSubject: PublishSubject<Int> = PublishSubject.create()

    init {
        repository.foldersSubject.subscribe {
            upcomingFoldersLiveData.postValue(it)
        }

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

    override fun refreshTripFolders() {
        repository.refreshTripFolders()
    }
}
