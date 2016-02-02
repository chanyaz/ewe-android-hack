package com.expedia.bookings.test

import android.text.SpannableStringBuilder
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DecimalNumberInputFilter
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class DecimalNumberInputFilterTests {

    @Test
    fun testInputUptoOneDecimalPlace() {
        val inputFilter: DecimalNumberInputFilter = DecimalNumberInputFilter(1)
        assertNull(inputFilter.filter("1", 0, 0, SpannableStringBuilder(""), 0, 0))
        assertNull(inputFilter.filter("2", 0, 0, SpannableStringBuilder("1"), 0, 0))
        assertNull(inputFilter.filter(".", 0, 0, SpannableStringBuilder("12"), 0, 0))
        assertNull(inputFilter.filter("2", 0, 0, SpannableStringBuilder("12."), 0, 0))
        assertEquals("", inputFilter.filter("2", 0, 0, SpannableStringBuilder("12.2"), 0, 0))
        assertEquals("", inputFilter.filter("5", 0, 0, SpannableStringBuilder("12.22"), 0, 0))
    }

    @Test
    fun testInputUptoTwoDecimalPlaces() {
        val inputFilter: DecimalNumberInputFilter = DecimalNumberInputFilter(2)
        assertNull(inputFilter.filter("1", 0, 0, SpannableStringBuilder(""), 0, 0))
        assertNull(inputFilter.filter("2", 0, 0, SpannableStringBuilder("1"), 0, 0))
        assertNull(inputFilter.filter(".", 0, 0, SpannableStringBuilder("12"), 0, 0))
        assertNull(inputFilter.filter("2", 0, 0, SpannableStringBuilder("12."), 0, 0))
        assertNull(inputFilter.filter("2", 0, 0, SpannableStringBuilder("12.2"), 0, 0))
        assertEquals("", inputFilter.filter("5", 0, 0, SpannableStringBuilder("12.22"), 0, 0))
        assertEquals("", inputFilter.filter("4", 0, 0, SpannableStringBuilder("12.22"), 0, 0))
    }
}