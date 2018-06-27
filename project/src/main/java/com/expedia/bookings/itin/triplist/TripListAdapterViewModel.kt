package com.expedia.bookings.itin.triplist

import com.expedia.bookings.data.trips.TripFolder
import io.reactivex.subjects.PublishSubject

class TripListAdapterViewModel : ITripListAdapterViewModel {
    override val upcomingTripFoldersSubject: PublishSubject<List<TripFolder>> = PublishSubject.create()
}

interface ITripListAdapterViewModel {
    val upcomingTripFoldersSubject: PublishSubject<List<TripFolder>>
}
