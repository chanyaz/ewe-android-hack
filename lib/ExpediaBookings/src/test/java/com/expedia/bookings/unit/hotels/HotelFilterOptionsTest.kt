package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelSearchParams
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelFilterOptionsTest {
    val testFilterOptions = HotelSearchParams.HotelFilterOptions()

    @Test
    fun testIsEmpty_hotelName() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterHotelName = "test"
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmpty_filterStarRatings() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterStarRatings = listOf(10)
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmpty_filterPrice() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterPrice = HotelSearchParams.PriceRange(10, 100)
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmpty_vipOnly() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterVipOnly = true
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmpty_sort() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.userSort = HotelSearchParams.SortType.MOBILE_DEALS
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testNotEmpty_hotelName() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterHotelName = "test"
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmpty_filterStarRatings() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterStarRatings = listOf(10)
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmpty_filterPrice() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterPrice = HotelSearchParams.PriceRange(10, 100)
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmpty_vipOnly() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterVipOnly = true
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmpty_sort() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.userSort = HotelSearchParams.SortType.MOBILE_DEALS
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmpty_amenities() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.amenities = hashSetOf(4, 16)
        assertTrue(testFilterOptions.isNotEmpty())
    }
}
