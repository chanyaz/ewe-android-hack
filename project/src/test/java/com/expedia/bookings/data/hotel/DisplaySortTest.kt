package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.BaseHotelFilterOptions
import org.junit.Test
import kotlin.test.assertEquals

class DisplaySortTest {

    @Test
    fun defaultSortIsRecommended() {
        assertEquals(DisplaySort.RECOMMENDED, DisplaySort.getDefaultSort())
    }

    @Test
    fun testFromServerSort() {
        assertEquals(DisplaySort.RECOMMENDED, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.EXPERT_PICKS))
        assertEquals(DisplaySort.PRICE, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.PRICE))
        assertEquals(DisplaySort.DEALS, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.MOBILE_DEALS))
        assertEquals(DisplaySort.PACKAGE_DISCOUNT, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.PACKAGE_SAVINGS))
        assertEquals(DisplaySort.RATING, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.REVIEWS))
        assertEquals(DisplaySort.DISTANCE, DisplaySort.fromServerSort(BaseHotelFilterOptions.SortType.DISTANCE))
    }
}
