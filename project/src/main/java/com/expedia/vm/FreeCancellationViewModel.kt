package com.expedia.vm

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FreeCancellationViewModel {
    val closeFreeCancellationObservable  = PublishSubject.create<Unit>()
    val freeCancellationTextObservable = BehaviorSubject.create<CharSequence>()
}