package com.expedia.bookings.itin.triplist.tripfolderlistitems

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.data.trips.TripFolderProduct
import com.expedia.bookings.itin.tripstore.extensions.abbreviatedDateRange
import com.expedia.bookings.itin.tripstore.extensions.endDate
import com.expedia.bookings.itin.tripstore.extensions.startDate
import com.expedia.bookings.itin.utils.StringSource
import io.reactivex.subjects.PublishSubject

class TripFolderItemViewModel(private val stringSource: StringSource) : ITripFolderItemViewModel {
    override val bindTripFolderSubject: PublishSubject<TripFolder> = PublishSubject.create()

    override val titleSubject: PublishSubject<String> = PublishSubject.create()
    override val timingSubject: PublishSubject<String> = PublishSubject.create()
    override val lobIconSubject: PublishSubject<List<TripFolderProduct>> = PublishSubject.create()

    init {
        bindTripFolderSubject.subscribe { folder ->
            titleSubject.onNext(folder.title)
            timingSubject.onNext(abbreviatedDateRange(folder.startDate(), folder.endDate(), stringSource))
            lobIconSubject.onNext(folder.lobs)
        }
    }
}

interface ITripFolderItemViewModel {
    val bindTripFolderSubject: PublishSubject<TripFolder>

    val titleSubject: PublishSubject<String>
    val timingSubject: PublishSubject<String>
    val lobIconSubject: PublishSubject<List<TripFolderProduct>>
}
