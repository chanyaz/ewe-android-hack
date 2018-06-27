package com.expedia.bookings.itin.triplist.upcoming

import com.expedia.bookings.data.trips.TripFolder
import io.reactivex.subjects.PublishSubject

class TripListTabViewModel : ITripListTabViewModel {
    override val foldersSubject: PublishSubject<List<TripFolder>> = PublishSubject.create()
}

interface ITripListTabViewModel {
    val foldersSubject: PublishSubject<List<TripFolder>>
}
