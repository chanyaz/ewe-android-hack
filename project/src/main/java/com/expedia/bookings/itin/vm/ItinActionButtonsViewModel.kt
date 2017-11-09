package com.expedia.bookings.itin.vm

import rx.Observable
import rx.subjects.PublishSubject

class ItinActionButtonsViewModel {
    val leftButtonVisibilityObservable = PublishSubject.create<Boolean>()
    val rightButtonVisibilityObservable = PublishSubject.create<Boolean>()
    val leftButtonTextObservable = PublishSubject.create<String>()
    val leftButtonDrawableObservable = PublishSubject.create<Int>()
    val rightButtonTextObservable = PublishSubject.create<String>()
    val rightButtonDrawableObservable = PublishSubject.create<Int>()
    val leftButtonClickedObservable = PublishSubject.create<Unit>()
    val rightButtonClickedObservable = PublishSubject.create<Unit>()
    val dividerVisibilityObservable = PublishSubject.create<Boolean>()
    
    init{
        Observable.zip(leftButtonVisibilityObservable, rightButtonVisibilityObservable, { leftButtonVisible: Boolean, rightButtonVisible: Boolean ->
            dividerVisibilityObservable.onNext(leftButtonVisible && rightButtonVisible)
        }).subscribe()
    }
}