package com.expedia.bookings.hotel.util

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFavoritesCacheTest {
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        HotelFavoritesCache.clearFavorites(context)
    }

    @Test
    fun testAddFavorite() {
        val hotelId = "HOTEL_1"
        HotelFavoritesCache.saveFavoriteId(context, hotelId)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "SomeOtherHotel"))
    }

    @Test
    fun testRemoveFavorite() {
        val hotelId = "HOTEL_1"
        HotelFavoritesCache.saveFavoriteId(context, hotelId)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
        HotelFavoritesCache.removeFavoriteId(context, hotelId)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
    }

    @Test
    fun testSaveFavorites() {
        val hotels = setOf("hotel1", "hotel2")
        HotelFavoritesCache.saveFavorites(context, hotels)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, "hotel1"))
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, "hotel2"))
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "hotel3"))
    }

    @Test
    fun testClearFavorites() {
        val hotels = setOf("hotel1", "hotel2")
        HotelFavoritesCache.saveFavorites(context, hotels)
        HotelFavoritesCache.clearFavorites(context)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "hotel1"))
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "hotel2"))
    }
}
