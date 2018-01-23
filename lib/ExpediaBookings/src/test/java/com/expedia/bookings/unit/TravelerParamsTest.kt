package com.expedia.bookings.unit

import com.expedia.bookings.data.TravelerParams
import org.junit.Test
import kotlin.test.assertEquals

class TravelerParamsTest {

    @Test
    fun testTravelerCount() {
        var travelerParams = TravelerParams(1, emptyList(), emptyList(), emptyList())
        assertEquals(1, travelerParams.getTravelerCount())

        travelerParams = TravelerParams(1, listOf(1), emptyList(), emptyList())
        assertEquals(2, travelerParams.getTravelerCount())

        travelerParams = TravelerParams(1, listOf(1), listOf(1), emptyList())
        assertEquals(3, travelerParams.getTravelerCount())

        travelerParams = TravelerParams(1, listOf(1), listOf(1), listOf(1, 2))
        assertEquals(5, travelerParams.getTravelerCount())
    }
}
