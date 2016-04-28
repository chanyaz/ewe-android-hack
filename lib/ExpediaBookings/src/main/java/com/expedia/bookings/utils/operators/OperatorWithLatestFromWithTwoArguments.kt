package com.expedia.bookings.utils.operators

import java.util.concurrent.atomic.AtomicReference

import rx.*
import rx.Observable.Operator
import rx.exceptions.Exceptions
import rx.functions.Func2
import rx.observers.SerializedSubscriber

class OperatorWithLatestFromWithTwoArguments<T, A, B, R>(val other1: Observable<A>, val other2: Observable<B>, val resultSelector: (T, A, B) -> R) : Operator<R, T> {
    override fun call(child: Subscriber<in R>): Subscriber<in T> {
        val s = SerializedSubscriber(child, false)
        child.add(s)

        var currentA = AtomicReference<Any>(EMPTY)
        var currentB = AtomicReference<Any>(EMPTY)

        val subscriber = object : Subscriber<T>(s, true) {
            override fun onNext(t: T) {
                if (currentA.get() !== EMPTY && currentB.get() !== EMPTY) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val result = resultSelector(t, currentA.get() as A, currentB.get() as B)
                        s.onNext(result)
                    } catch (e: Throwable) {
                        Exceptions.throwOrReport(e, this)
                    }
                }
            }

            override fun onError(e: Throwable) {
                s.onError(e)
                s.unsubscribe()
            }

            override fun onCompleted() {
                s.onCompleted()
                s.unsubscribe()
            }
        }

        s.add(subscriber)

        //First observable
        val otherSubscriber1 = subscribeTemp<A>(currentA, s)
        val otherSubscriber2 = subscribeTemp<B>(currentB, s)

        s.add(otherSubscriber1)
        s.add(otherSubscriber2)

        other1.unsafeSubscribe(otherSubscriber1)
        other2.unsafeSubscribe(otherSubscriber2)

        return subscriber
    }

    private fun <G> subscribeTemp(current: AtomicReference<Any>, s: SerializedSubscriber<R>): Subscriber<G> {
        return object : Subscriber<G>() {
            override fun onNext(t: G) {
                current.set(t)
            }

            override fun onError(e: Throwable) {
                s.onError(e)
                s.unsubscribe()
            }

            override fun onCompleted() {
                if (current.get() === EMPTY) {
                    s.onCompleted()
                    s.unsubscribe()
                }
            }
        }
    }

    companion object {
        /** Indicates the other has not yet emitted a value.  */
        internal val EMPTY = Object()
    }
}
