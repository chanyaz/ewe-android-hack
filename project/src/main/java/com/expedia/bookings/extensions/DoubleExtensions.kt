package com.expedia.bookings.extensions

fun Double.isWholeNumber(): Boolean {
    return (this - this.toInt()) == 0.0
}
