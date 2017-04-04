package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
        filters1.userSort = Sort.DISTANCE
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
}