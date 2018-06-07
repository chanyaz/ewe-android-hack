package com.expedia.bookings.itin.triplist

import io.reactivex.subjects.PublishSubject

interface ITripListFragmentViewModel {

    val tripListVisitTrackingSubject: PublishSubject<Int>
    val tabSelectedSubject: PublishSubject<Int>
}
