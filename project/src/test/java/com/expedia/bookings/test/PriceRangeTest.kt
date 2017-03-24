package com.expedia.bookings.test

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.data.hotel.PriceRange
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PriceRangeTest {
    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun priceRange() {
        val range = PriceRange("USD", 0, 300)
        assertEquals("$0", range.defaultMinPriceText)
        assertEquals("$300+", range.defaultMaxPriceText)
        assertEquals("$0", range.formatValue(0))
        assertEquals("$10", range.formatValue(1))
        assertEquals("$300+", range.formatValue(range.notches))
    }
}
