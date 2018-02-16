package com.expedia.bookings.unit.travelgraph

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.data.travelgraph.TravelerInfo
import org.junit.Assert.assertTrue
import org.joda.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class SearchInfoTest {
    private lateinit var destination: SuggestionV4

    @Before
    fun setUp() {
        destination = SuggestionV4()
        destination.gaiaId = "123"
    }

    @Test
    fun testInvalidDatesIfInThePast() {
        val now = LocalDate.now()
        val start = now.minusDays(2)
        val end = now.minusDays(1)

        val searchInfo = SearchInfo(destination, start, end, TravelerInfo())
        assertFalse(searchInfo.isValid())
    }

    @Test
    fun testInvalidDatesIfEndBeforeStart() {
        val now = LocalDate.now()
        val start = now.plusDays(2)
        val end = now.plusDays(1)

        val searchInfo = SearchInfo(destination, start, end, TravelerInfo())
        assertFalse(searchInfo.isValid())
    }

    @Test
    fun testValidDates() {
        val now = LocalDate.now()
        val start = now.plusDays(1)
        val end = now.plusDays(2)

        val searchInfo = SearchInfo(destination, start, end, TravelerInfo())
        assertTrue(searchInfo.isValid())
    }

    @Test
    fun testValidDatesIfSameStartAndEnd() {
        val now = LocalDate.now()
        val start = now.plusDays(1)
        val end = now.plusDays(1)

        val searchInfo = SearchInfo(destination, start, end, TravelerInfo())
        assertTrue(searchInfo.isValid())
    }
}
