package com.expedia.bookings.data

class TravelerParams(val numberOfAdults: Int, val childrenAges: List<Int>, val youthAges: List<Int>, val seniorAges: List<Int>) {

    fun getTravelerCount(): Int {
        return numberOfAdults + seniorAges.size + youthAges.size + childrenAges.size
    }
}
