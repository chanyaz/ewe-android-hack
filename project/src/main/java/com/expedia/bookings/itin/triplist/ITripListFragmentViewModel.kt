package com.expedia.bookings.itin.triplist

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.data.trips.TripFolder
import io.reactivex.subjects.PublishSubject

interface ITripListFragmentViewModel {

    val upcomingFoldersLiveData: MutableLiveData<List<TripFolder>>
    val tripListVisitTrackingSubject: PublishSubject<Int>
    val tabSelectedSubject: PublishSubject<Int>

    fun refreshTripFolders()
}
