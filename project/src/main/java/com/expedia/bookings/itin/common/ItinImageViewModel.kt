package com.expedia.bookings.itin.common

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

abstract class ItinImageViewModel {
    val imageUrlSubject: PublishSubject<String> = PublishSubject.create()
    val nameSubject: PublishSubject<String> = PublishSubject.create()
    abstract val itinObserver: LiveDataObserver<Itin>
}
