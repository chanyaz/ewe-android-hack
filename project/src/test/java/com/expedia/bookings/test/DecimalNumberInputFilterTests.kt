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
        assertNull(inputFilter.filter("1", 0, 1, SpannableStringBuilder(""), 0, 0))
        assertNull(inputFilter.filter("2", 0, 1, SpannableStringBuilder("1"), 1, 1))
        assertNull(inputFilter.filter(".", 0, 1, SpannableStringBuilder("12"), 2, 2))
        assertNull(inputFilter.filter("2", 0, 1, SpannableStringBuilder("12."), 3, 3))
        assertEquals("", inputFilter.filter("2", 0, 1, SpannableStringBuilder("12.2"), 4, 4))
        assertEquals("", inputFilter.filter("5", 0, 1, SpannableStringBuilder("12.22"), 4, 4))
        //Enter something before decimals
        assertNull(inputFilter.filter("8", 0, 1, SpannableStringBuilder("12.2"), 1, 1))
        assertNull(inputFilter.filter("6", 0, 1, SpannableStringBuilder("182.2"), 0, 0))
    }

    @Test
    fun testInputUptoTwoDecimalPlaces() {
        val inputFilter: DecimalNumberInputFilter = DecimalNumberInputFilter(2)
        assertNull(inputFilter.filter("1", 0, 1, SpannableStringBuilder(""), 0, 0))
        assertNull(inputFilter.filter("2", 0, 1, SpannableStringBuilder("1"), 1, 1))
        assertNull(inputFilter.filter(".", 0, 1, SpannableStringBuilder("12"), 2, 2))
        assertNull(inputFilter.filter("2", 0, 1, SpannableStringBuilder("12."), 3, 3))
        assertNull(inputFilter.filter("2", 0, 1, SpannableStringBuilder("12.2"), 4, 4))
        assertEquals("", inputFilter.filter("5", 0, 1, SpannableStringBuilder("12.22"), 5, 5))
        assertEquals("", inputFilter.filter("4", 0, 1, SpannableStringBuilder("12.22"), 5, 5))
        //Enter something before decimals
        assertNull(inputFilter.filter("8", 0, 1, SpannableStringBuilder("12.22"), 1, 1))
        assertNull(inputFilter.filter("6", 0, 1, SpannableStringBuilder("182.22"), 0, 0))
    }
}
