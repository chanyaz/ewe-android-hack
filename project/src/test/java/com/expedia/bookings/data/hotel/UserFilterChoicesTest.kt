package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class UserFilterChoicesTest {
    lateinit var filters1: UserFilterChoices
    lateinit var filters2: UserFilterChoices

    @Before
    fun setUp() {
        filters1 = UserFilterChoices()
        filters2 = UserFilterChoices()
        assertTrue(filters1 == filters2)
    }

    @Test
    fun testCompareStarRatings() {
        val stars1 = UserFilterChoices.StarRatings()
        stars1.one = true
        filters1.hotelStarRating = stars1

        val stars2 = UserFilterChoices.StarRatings()
        stars2.one = true
        filters2.hotelStarRating = stars2

        assertTrue(filters1 == filters2)

        stars1.two = true
        assertFalse(filters1 == filters2)
    }

    @Test
    fun testStarRatingsClone() {
        val stars1 = UserFilterChoices.StarRatings()
        stars1.one = true
        filters1.hotelStarRating = stars1
        val f = filters1.copy()

        assertTrue(filters1 == f)

        stars1.two = true
        assertFalse(filters1 == f)
    }

    @Test
    fun testCompareHotelName() {
        filters1.name = "Hyatt"
        filters2.name = "Hyatt"
        assertTrue(filters1 == filters2)
        filters1.name = ""
        assertFalse(filters1 == filters2)
    }

    @Test
    fun testCompareSort() {
        filters1.userSort = DisplaySort.DISTANCE
        assertFalse(filters1 == filters2)
    }

    @Test
    fun testNeighborhoodsClone() {
        val neighborhood1 = HotelSearchResponse.Neighborhood()
        neighborhood1.id = "id1"
        neighborhood1.name = "Nice neighborhood"

        val neighborhood2 = HotelSearchResponse.Neighborhood()
        neighborhood2.id = "id2"
        neighborhood2.name = "Don't stay here"

        filters1.neighborhoods = hashSetOf(neighborhood1, neighborhood2)

        val filters = filters1.copy()
        assertTrue(filters1 == filters)

        filters1.neighborhoods.remove(neighborhood1)
        assertFalse(filters1 == filters)
    }

    @Test
    fun testFromHotelFilterOptions() {
        val hotelName = "Dingy Paradise"
        val searchOptions = HotelSearchParams.HotelFilterOptions()
        searchOptions.filterHotelName = hotelName
        searchOptions.filterStarRatings = listOf(10, 40, 50)
        searchOptions.userSort = HotelSearchParams.SortType.DISTANCE
        searchOptions.filterVipOnly = true
        searchOptions.amenities = hashSetOf(1, 3)

        val filterOptions = UserFilterChoices.fromHotelFilterOptions(searchOptions)
        assertEquals(hotelName, filterOptions.name)
        assertEquals(DisplaySort.DISTANCE, filterOptions.userSort)
        assertTrue(filterOptions.isVipOnlyAccess)
        assertTrue(filterOptions.hotelStarRating.one)
        assertTrue(filterOptions.hotelStarRating.four)
        assertTrue(filterOptions.hotelStarRating.five)

        assertFalse(filterOptions.hotelStarRating.two)
        assertFalse(filterOptions.hotelStarRating.three)
        assertTrue(filterOptions.amenities.contains(1))
        assertTrue(filterOptions.amenities.contains(3))
    }

    @Test
    fun testFromEmptyHotelFilterOptions() {
        val searchOptions = HotelSearchParams.HotelFilterOptions()

        val filterOptions = UserFilterChoices.fromHotelFilterOptions(searchOptions)
        assertEquals("", filterOptions.name)
        assertEquals(DisplaySort.RECOMMENDED, filterOptions.userSort)
        assertFalse(filterOptions.isVipOnlyAccess)
        val starRating = UserFilterChoices.StarRatings()
        assertEquals(starRating, filterOptions.hotelStarRating)
    }
}
