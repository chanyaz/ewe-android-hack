package com.expedia.bookings.itin.common

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import io.reactivex.subjects.PublishSubject

abstract class ItinImageViewModel<T : ItinLOB> {
    val imageUrlSubject: PublishSubject<String> = PublishSubject.create()
    val nameSubject: PublishSubject<String> = PublishSubject.create()
    abstract val itinLOBObserver: LiveDataObserver<T>
}
