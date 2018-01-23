package com.expedia.bookings.data.hotel

import org.junit.Test
import kotlin.test.assertEquals

class DisplaySortTest {

    @Test
    fun defaultSortIsRecommended() {
        assertEquals(DisplaySort.RECOMMENDED, DisplaySort.getDefaultSort())
    }
}
