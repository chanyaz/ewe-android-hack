package com.expedia.bookings.itin.triplist

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.expedia.bookings.tracking.ITripsTracking

class TripListFragmentViewModelFactory(private val tripsTracking: ITripsTracking, private val repository: ITripListRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TripListFragmentViewModel(tripsTracking, repository) as T
    }
}
