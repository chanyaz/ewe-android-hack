package com.expedia.bookings.itin.common

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

abstract class ItinTimingsWidgetViewModel {
    val endTitleSubject: PublishSubject<String> = PublishSubject.create()
    val startTitleSubject: PublishSubject<String> = PublishSubject.create()
    val endDateSubject: PublishSubject<String> = PublishSubject.create()
    val startDateSubject: PublishSubject<String> = PublishSubject.create()
    val endTimeSubject: PublishSubject<String> = PublishSubject.create()
    val startTimeSubject: PublishSubject<String> = PublishSubject.create()

    abstract val itinObserver: LiveDataObserver<Itin>
}
