package com.expedia.bookings.vm

import io.reactivex.subjects.PublishSubject

class DebugSelectLanguageVM {
    val restartAppSubject = PublishSubject.create<Unit>()
}
