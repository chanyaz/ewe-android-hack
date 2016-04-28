package com.expedia.bookings.utils.operators

import rx.Observable
import rx.Subscriber

class OperatorDistinctUntilChangedWithComparer<T>(val comparer: (T?, T?) -> Boolean) : Observable.Operator<T, T> {
    override fun call(child: Subscriber<in T>): Subscriber<in T>? {
        return object : Subscriber<T>(child) {
            var previousKey: T? = null
            var hasPrevious: Boolean = false
            override fun onNext(t: T) {
                if (hasPrevious) {
                    if (!comparer(previousKey, t)) {
                        child.onNext(t)
                    } else {
                        request(1)
                    }
                } else {
                    hasPrevious = true
                    child.onNext(t)
                }

                previousKey = t
            }

            override fun onError(e: Throwable?) {
                child.onError(e)
            }

            override fun onCompleted() {
                child.onCompleted()
            }
        }
    }
}