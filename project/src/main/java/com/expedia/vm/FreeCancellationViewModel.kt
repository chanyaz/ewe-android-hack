package com.expedia.vm

import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FreeCancellationViewModel {
    val closeFreeCancellationObservable  = PublishSubject.create<Unit>()
    val freeCancellationTextObservable = BehaviorSubject.create<CharSequence>()
}