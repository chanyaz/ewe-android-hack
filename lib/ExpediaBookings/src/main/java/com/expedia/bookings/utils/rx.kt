package com.expedia.bookings.utils

import com.expedia.bookings.utils.operators.OperatorDistinctUntilChangedWithComparer
import rx.Observable
import com.expedia.bookings.utils.operators.OperatorWithLatestFromWithMultipleArguments
import com.expedia.bookings.utils.operators.OperatorWithLatestFromWithTwoArguments

/**
 *  Returns an observable sequence that contains only distinct contiguous elements according to the comparer.
 *
 *  var obs = observable.distinctUntilChanged{ x, y-> x == y };
 *
 * @param {Function} [comparer] Equality comparer for computed key values. If not provided, defaults to an equality comparer function.
 * @returns {Observable} An observable sequence only containing the distinct contiguous elements, based on a computed key value, from the source sequence.
 */
fun <T> Observable<T>.distinctUntilChanged(comparer: (T?, T?) -> Boolean): Observable<T> {
    return lift(OperatorDistinctUntilChangedWithComparer(comparer))
}

fun <T, A, B, C, R> Observable<T>.withLatestFrom(other1: Observable<A>, other2: Observable<B>, other3: Observable<C>, resultSelector: (T, A, B, C) -> R): Observable<R> {
    return lift(OperatorWithLatestFromWithMultipleArguments<T, A, B, C, R>(other1, other2, other3, resultSelector))
}

fun <T, A, B, R> Observable<T>.withLatestFrom(other1: Observable<A>, other2: Observable<B>, resultSelector: (T, A, B) -> R): Observable<R> {
    return lift(OperatorWithLatestFromWithTwoArguments<T, A, B, R>(other1, other2, resultSelector))
}