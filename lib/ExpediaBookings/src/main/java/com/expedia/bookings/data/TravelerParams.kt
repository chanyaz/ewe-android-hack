package com.expedia.bookings.data

class TravelerParams(val numberOfAdults: Int, val childrenAges: List<Int>, val youthAges: List<Int>, val seniorAges: List<Int>) {

    fun getTravelerCount(): Int {
        return numberOfAdults + seniorAges.size + youthAges.size + childrenAges.size
    }

    fun equalParams(other: TravelerParams): Boolean {
        return this.numberOfAdults == other.numberOfAdults && compareLists(this.childrenAges, other.childrenAges) &&
                compareLists(this.youthAges, other.youthAges) && compareLists(this.seniorAges, other.seniorAges)
    }

    fun compareLists(list1: List<Int>, list2: List<Int>): Boolean {
        if (list1.size == list2.size) {
            for (index in list1.indices) {
                if (list1[index] != list2[index]) {
                    return false
                }
            }
        } else {
            return false
        }
        return true
    }
}
