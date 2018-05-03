package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.packages.PackageHotelFilterOptions
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PackageHotelFilterOptionsTest {
    val testFilterOptions = PackageHotelFilterOptions()

    @Test
    fun testIsEmptyHotelNameFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterHotelName = "test"
        assertFalse(testFilterOptions.isEmpty())
    }

    @Test
    fun testIsEmptyStarRatingsFilter() {
        assertTrue(testFilterOptions.isEmpty()) // sanity check

        testFilterOptions.filterStarRatings = listOf(1)
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

        testFilterOptions.userSort = BaseHotelFilterOptions.SortType.MOBILE_DEALS
        assertFalse(testFilterOptions.isEmpty())
    }
}
