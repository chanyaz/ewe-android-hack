package com.expedia.bookings.services

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

fun <T> Observable<T>.subscribeTestObserver(observer: TestObserver<T>): Disposable {
    return subscribe({ observer.onNext(it) }, { observer.onError(it) }, { observer.onComplete() })
}
