package com.expedia.bookings.test

import com.expedia.vm.HotelFilterViewModel
import org.junit.Test
import kotlin.test.assertEquals

class PriceRangeTest {
    @Test fun priceRange() {
        val range = HotelFilterViewModel.PriceRange("USD", 0, 300)
        assertEquals("$0", range.defaultMinPriceText)
        assertEquals("$300+", range.defaultMaxPriceText)
        assertEquals("$0", range.formatValue(0))
        assertEquals("$10", range.formatValue(1))
        assertEquals("$300+", range.formatValue(range.notches))
    }
}
