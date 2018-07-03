package com.expedia.layouttestandroid.extension

fun <T> Collection<List<T>>.combine(other: Iterable<List<T>>): List<List<T>> {
    return combine(other, { thisItem: List<T>, otherItem: List<T> ->
        val list = ArrayList<T>()
        list.addAll(thisItem)
        list.addAll(otherItem)
        list
    })
}

fun <T1, T2, R> Collection<T1>.combine(other: Iterable<T2>, transformer: (thisItem: T1, otherItem: T2) -> R): List<R> {
    return this.flatMap { thisItem -> other.map { otherItem -> transformer(thisItem, otherItem) } }
}

