package com.expedia.bookings.unit

import com.expedia.bookings.data.lx.LXThemeType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LXThemeTypeTest {

    @Test
    fun testTheme() {
        val adventureAround = LXThemeType.AdventureAround
        assertEquals(3, adventureAround.categories.size)

        assertEquals("Adventures", adventureAround.categories.get(0).toString())
        assertEquals("DayTripsExcursions", adventureAround.categories.get(1).toString())
        assertEquals("MultiDayExtendedTours", adventureAround.categories.get(2).toString())
    }

    @Test
    fun testAllThingsToDoTheme() {
        val allThingsToDo = LXThemeType.AllThingsToDo
        assertEquals(0, allThingsToDo.categories.size)
    }

    @Test
    fun testTopRatedActivityTheme() {
        val topRatedTheme = LXThemeType.TopRatedActivities
        assertEquals(0, topRatedTheme.categories.size)
    }
}