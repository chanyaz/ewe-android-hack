package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

abstract class ItinImageViewModel {
    val imageUrlSubject: PublishSubject<String> = PublishSubject.create()
    val nameSubject: PublishSubject<String> = PublishSubject.create()
}
