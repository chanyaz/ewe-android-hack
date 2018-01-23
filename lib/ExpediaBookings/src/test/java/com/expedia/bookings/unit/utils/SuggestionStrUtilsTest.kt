package com.expedia.bookings.unit.utils

import com.expedia.bookings.utils.SuggestionStrUtils
import org.junit.Test
import kotlin.test.assertEquals

class SuggestionStrUtilsTest {

    @Test
    fun testFormatDashWithoutSpace() {
        var input = "NoDash"
        assertEquals(input, SuggestionStrUtils.formatDashWithoutSpace(input))

        input = "Dash - with space"
        assertEquals(input, SuggestionStrUtils.formatDashWithoutSpace(input))

        input = "Dash-without space"
        assertEquals("Dash - without space", SuggestionStrUtils.formatDashWithoutSpace(input))
    }

    @Test
    fun testFormatAirportName() {
        val input = "Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah)"
        assertEquals("Kuantan, Malaysia (KUA - Sultan Haji Ahmad Shah)", SuggestionStrUtils.formatDashWithoutSpace(input))
    }
}
