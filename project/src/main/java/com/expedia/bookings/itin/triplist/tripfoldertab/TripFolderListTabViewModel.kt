package com.expedia.bookings.itin.triplist.tripfoldertab

import com.expedia.bookings.data.trips.TripFolder
import io.reactivex.subjects.PublishSubject

class TripFolderListTabViewModel : ITripFolderListTabViewModel {
    override val foldersSubject: PublishSubject<List<TripFolder>> = PublishSubject.create()
}

interface ITripFolderListTabViewModel {
    val foldersSubject: PublishSubject<List<TripFolder>>
}
