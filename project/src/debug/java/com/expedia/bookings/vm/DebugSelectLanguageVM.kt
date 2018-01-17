package com.expedia.bookings.vm

import rx.subjects.PublishSubject

class DebugSelectLanguageVM {
    val restartAppSubject = PublishSubject.create<Unit>()
}
