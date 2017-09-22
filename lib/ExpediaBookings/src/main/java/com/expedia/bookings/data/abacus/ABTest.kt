package com.expedia.bookings.data.abacus

data class ABTest(val key: Int, val remote: Boolean) {
    constructor(key: Int) : this(key, false)
}