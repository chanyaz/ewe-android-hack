package com.expedia.bookings

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.functions.Function5
import io.reactivex.functions.Function6
import io.reactivex.functions.Function8

fun <T> Observable<T>.subscribeObserver(observer: Observer<T>): Disposable {
    return subscribe({ observer.onNext(it) }, { observer.onError(it) }, { observer.onComplete() })
}

fun <T> Single<T>.subscribe(observer: Observer<T>): Disposable {
    return subscribe({ observer.onNext(it) }, { observer.onError(it) })
}

fun <T1, T2, R> Observable<T1>.withLatestFrom(other: Observable<T2>, block: (T1, T2) -> R): Observable<R> {
    return withLatestFrom(other, BiFunction<T1, T2, R> { t1, t2 -> block(t1, t2) })
}

fun <T1, T2, T3, R> Observable<T1>.withLatestFrom(o1: Observable<T2>, o2: Observable<T3>, block: (T1, T2, T3) -> R): Observable<R> {
    return withLatestFrom(o1, o2, Function3<T1, T2, T3, R> { t1, t2, t3 -> block(t1, t2, t3) })
}

fun <T1, T2, T3, T4, R> Observable<T1>.withLatestFrom(o1: Observable<T2>, o2: Observable<T3>, o3: Observable<T4>, block: (T1, T2, T3, T4) -> R): Observable<R> {
    return withLatestFrom(o1, o2, o3, Function4 { t1, t2, t3, t4 -> block(t1, t2, t3, t4) })
}

fun <T1, T2, R> Observable<T1>.zipWith(other: Observable<T2>, block: (T1, T2) -> R): Observable<R> {
    return zipWith(other, BiFunction<T1, T2, R> { t1, t2 -> block(t1, t2) })
}

object ObservableOld {
    fun <T1, T2, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, block: (T1, T2) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, BiFunction { t1, t2 -> block(t1, t2) })
    }

    fun <T1, T2, T3, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, block: (T1, T2, T3) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, o3, Function3 { t1, t2, t3 -> block(t1, t2, t3) })
    }

    fun <T1, T2, T3, T4, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, o4: Observable<T4>, block: (T1, T2, T3, T4) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, o3, o4, Function4 { t1, t2, t3, t4 -> block(t1, t2, t3, t4) })
    }

    fun <T1, T2, T3, T4, T5, T6, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, o4: Observable<T4>, o5: Observable<T5>, o6: Observable<T6>, block: (T1, T2, T3, T4, T5, T6) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, o3, o4, o5, o6, Function6 { t1, t2, t3, t4, t5, t6 -> block(t1, t2, t3, t4, t5, t6) })
    }

    fun <T1, T2, T3, T4, T5, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, o4: Observable<T4>, o5: Observable<T5>, block: (T1, T2, T3, T4, T5) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, o3, o4, o5, Function5 { t1, t2, t3, t4, t5 -> block(t1, t2, t3, t4, t5) })
    }

    fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, o4: Observable<T4>, o5: Observable<T5>, o6: Observable<T6>, o7: Observable<T7>, o8: Observable<T8>, block: (T1, T2, T3, T4, T5, T6, T7, T8) -> R): Observable<R> {
        return Observable.combineLatest(o1, o2, o3, o4, o5, o6, o7, o8, Function8 { t1, t2, t3, t4, t5, t6, t7, t8 -> block(t1, t2, t3, t4, t5, t6, t7, t8) })
    }

    fun <T1, T2, R> zip(o1: Observable<T1>, o2: Observable<T2>, block: (T1, T2) -> R): Observable<R> {
        return Observable.zip(o1, o2, BiFunction { t1, t2 -> block(t1, t2) })
    }

    fun <T1, T2, T3, R> zip(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, block: (T1, T2, T3) -> R): Observable<R> {
        return Observable.zip(o1, o2, o3, Function3 { t1, t2, t3 -> block(t1, t2, t3) })
    }
}
