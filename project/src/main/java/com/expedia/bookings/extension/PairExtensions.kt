package com.expedia.bookings.extension

operator fun <F, S> android.util.Pair<F, S>.component1(): F {
    return first
}

operator fun <F, S> android.util.Pair<F, S>.component2(): S {
    return second
}
