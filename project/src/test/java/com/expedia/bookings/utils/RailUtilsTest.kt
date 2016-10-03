package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailUtilsTest {
    val context = RuntimeEnvironment.application

    @Test
    fun testRailChangeTextNoChange() {
        val noChangeText = RailUtils.formatRailChangesText(context, 0)
        assertEquals(context.getString(R.string.rail_direct), noChangeText)
    }

    @Test
    fun testRailChangeTextSingular() {
        val oneChangeText = RailUtils.formatRailChangesText(context, 1)
        assertEquals("1 Change", oneChangeText)
    }

    @Test
    fun testRailChangeTextPlural() {
        val pluralChangeText = RailUtils.formatRailChangesText(context, 3)
        assertEquals("3 Changes", pluralChangeText)
    }
}