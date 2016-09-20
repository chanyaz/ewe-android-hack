package com.expedia.bookings.utils

import com.expedia.bookings.utils.operators.OperatorWithLatestFromWithMultipleArguments
import com.expedia.bookings.utils.operators.OperatorWithLatestFromWithTwoArguments
import rx.Observable

fun <T, A, B, C, R> Observable<T>.withLatestFrom(other1: Observable<A>, other2: Observable<B>, other3: Observable<C>, resultSelector: (T, A, B, C) -> R): Observable<R> {
    return lift(OperatorWithLatestFromWithMultipleArguments<T, A, B, C, R>(other1, other2, other3, resultSelector))
}

fun <T, A, B, R> Observable<T>.withLatestFrom(other1: Observable<A>, other2: Observable<B>, resultSelector: (T, A, B) -> R): Observable<R> {
    return lift(OperatorWithLatestFromWithTwoArguments<T, A, B, R>(other1, other2, resultSelector))
}