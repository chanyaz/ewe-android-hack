package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.Neighborhood
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
    fun testCompareGuestRatings() {
        val guestRating1 = UserFilterChoices.GuestRatings()
        guestRating1.three = true
        filters1.hotelGuestRating = guestRating1

        val guestRating2 = UserFilterChoices.GuestRatings()
        guestRating2.three = true
        filters2.hotelGuestRating = guestRating2

        assertTrue(filters1 == filters2)

        guestRating1.four = true
        assertFalse(filters1 == filters2)
    }

    @Test
    fun testGuestRatingsClone() {
        val guestRating1 = UserFilterChoices.GuestRatings()
        guestRating1.three = true
        filters1.hotelGuestRating = guestRating1
        val f = filters1.copy()

        assertTrue(filters1 == f)

        guestRating1.four = true
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
        val neighborhood1 = Neighborhood()
        neighborhood1.id = "id1"
        neighborhood1.name = "Nice neighborhood"

        val neighborhood2 = Neighborhood()
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
        searchOptions.filterPrice = HotelSearchParams.PriceRange(10, 20)
        val neighborhood = Neighborhood()
        neighborhood.name = "name"
        neighborhood.id = "id"
        searchOptions.filterByNeighborhood = neighborhood

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

        assertEquals(10, filterOptions.minPrice)
        assertEquals(20, filterOptions.maxPrice)
        assertEquals(1, filterOptions.neighborhoods.count())
        assertEquals("name", filterOptions.neighborhoods.first().name)
        assertEquals("id", filterOptions.neighborhoods.first().id)
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
        val guestRating = UserFilterChoices.GuestRatings()
        assertEquals(guestRating, filterOptions.hotelGuestRating)
        assertEquals(0, filterOptions.minPrice)
        assertEquals(0, filterOptions.maxPrice)
        assertTrue(filterOptions.neighborhoods.isEmpty())
    }
}
