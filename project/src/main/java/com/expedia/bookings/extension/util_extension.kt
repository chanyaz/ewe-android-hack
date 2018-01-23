package com.expedia.bookings.extension

fun Double.isWholeNumber(): Boolean {
    return (this - this.toInt()) == 0.0
}
