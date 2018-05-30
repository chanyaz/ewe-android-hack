package com.expedia.bookings.hotel.util

import android.content.Context
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
        clearCache()
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

    private fun clearCache() {
        val cache = context.getSharedPreferences(HotelFavoritesCache.FAVORITES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = cache.edit()
        editor.clear()
    }
}
