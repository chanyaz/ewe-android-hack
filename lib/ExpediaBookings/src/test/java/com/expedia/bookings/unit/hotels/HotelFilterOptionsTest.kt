package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelSearchParams
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelFilterOptionsTest {
    val testFilterOptions = HotelSearchParams.HotelFilterOptions()

    @Test
    fun testIsEmptyHotelNameFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterHotelName = "test"
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptyStarRatingsFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterStarRatings = listOf(10)
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptyGuestRatingsFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterGuestRatings = listOf(5)
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptyPriceFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterPrice = HotelSearchParams.PriceRange(10, 100)
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptyVipOnlyFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterVipOnly = true
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptySort() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.userSort = HotelSearchParams.SortType.MOBILE_DEALS
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testNotEmptyHotelNameFilter() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterHotelName = "test"
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptyStarRatingsFilter() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterStarRatings = listOf(10)
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptyGuestRatingsFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterGuestRatings = listOf(5)
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptyPriceFilter() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterPrice = HotelSearchParams.PriceRange(10, 100)
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptyVipOnlyFilter() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.filterVipOnly = true
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptySort() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.userSort = HotelSearchParams.SortType.MOBILE_DEALS
        assertTrue(testFilterOptions.isNotEmpty())
    }

    @Test
    fun testNotEmptyAmenitiesFilter() {
        assertFalse(testFilterOptions.isNotEmpty()) // sanity check

        testFilterOptions.amenities = hashSetOf(4, 16)
        assertTrue(testFilterOptions.isNotEmpty())
    }
}
