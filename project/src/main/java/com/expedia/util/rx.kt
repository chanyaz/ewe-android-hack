package com.expedia.util

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException

fun <T> endlessObserver(body: (T) -> Unit): Observer<T> {
    return object : Observer<T> {
        override fun onSubscribe(d: Disposable) {
            //ignore
        }

        override fun onNext(t: T) {
            body(t)
        }

        override fun onComplete() {
            throw OnErrorNotImplementedException(RuntimeException("Cannot call completed on endless observer " + body.javaClass))
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException("Error at " + body.javaClass, e)
        }
    }
}
