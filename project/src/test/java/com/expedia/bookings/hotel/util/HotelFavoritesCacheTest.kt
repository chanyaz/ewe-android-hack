package com.expedia.bookings.hotel.util

import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
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
    fun testSaveFavoriteId() {
        val hotelId = "HOTEL_1"

        assertSaveFavoriteId(hotelId, setOf(hotelId))
    }

    @Test
    fun testSaveFavoriteIdSameIdMultipleTimes() {
        val hotelId = "1_LETOH"

        assertSaveFavoriteId(hotelId, setOf(hotelId))
        assertSaveFavoriteId(hotelId, setOf(hotelId))
        assertSaveFavoriteId(hotelId, setOf(hotelId))
    }

    @Test
    fun testSaveFavoriteIdEmptyId() {
        val hotelId = ""

        assertSaveFavoriteId(hotelId, setOf(hotelId))
    }

    @Test
    fun testSaveFavoriteIdBlankId() {
        val hotelId = " "

        assertSaveFavoriteId(hotelId, setOf(hotelId))
    }

    @Test
    fun testRemoveFavoriteId() {
        val hotelIdsToSave = setOf("HOTEL_1", "HOTEL_2")
        val hotelIdToRemove = "HOTEL_1"

        assertRemoveFavoriteId(hotelIdsToSave, hotelIdToRemove, setOf("HOTEL_2"))
    }

    @Test
    fun testRemoveFavoriteIdFromOneFavorite() {
        val hotelIdsToSave = setOf("HOTEL_1")
        val hotelIdToRemove = "HOTEL_1"

        assertRemoveFavoriteId(hotelIdsToSave, hotelIdToRemove, emptySet())
    }

    @Test
    fun testRemoveFavoriteIdNotInFavorites() {
        val hotelIdsToSave = setOf("HOTEL_1")
        val hotelIdToRemove = "HOTEL_2"

        assertRemoveFavoriteId(hotelIdsToSave, hotelIdToRemove, hotelIdsToSave)
    }

    @Test
    fun testRemoveFavoriteIdEmptyHotelId() {
        val hotelIdsToSave = setOf("")
        val hotelIdToRemove = ""

        assertRemoveFavoriteId(hotelIdsToSave, hotelIdToRemove, emptySet())
    }

    @Test
    fun testRemoveFavoriteIdBlankHotelId() {
        val hotelIdsToSave = setOf(" ")
        val hotelIdToRemove = " "

        assertRemoveFavoriteId(hotelIdsToSave, hotelIdToRemove, emptySet())
    }

    @Test
    fun testIsFavoriteHotel() {
        val hotelId = "HOTEL_1"
        HotelFavoritesCache.saveFavoriteId(context, hotelId)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
    }

    @Test
    fun testIsFavoriteHotelOnEmptyFavorites() {
        val hotelId = "HOTEL_1"
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
    }

    @Test
    fun testIsNotFavoriteHotel() {
        val hotelId = "HOTEL_1"
        HotelFavoritesCache.saveFavoriteId(context, hotelId)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "HOTEL_2"))
    }

    @Test
    fun testSaveFavorites() {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)

        val hotels = setOf("hotel1", "hotel2")
        HotelFavoritesCache.saveFavorites(context, hotels)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, "hotel1"))
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, "hotel2"))
        testObserver.assertValueCount(1)
        assertEquals(hotels, testObserver.values()[0])
    }

    @Test
    fun testSaveFavoritesEmptySet() {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)

        HotelFavoritesCache.saveFavorites(context, emptySet())
        testObserver.assertValueCount(1)
        assertEquals(emptySet(), testObserver.values()[0])
    }

    @Test
    fun testClearFavorites() {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)
        val hotels = setOf("hotel1", "hotel2")
        HotelFavoritesCache.saveFavorites(context, hotels)
        HotelFavoritesCache.clearFavorites(context)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "hotel1"))
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, "hotel2"))
        testObserver.assertValueCount(2)
        assertEquals(hotels, testObserver.values()[0])
        assertEquals(emptySet(), testObserver.values()[1])
    }

    @Test
    fun testClearFavoritesEmptySet() {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)

        HotelFavoritesCache.clearFavorites(context)
        testObserver.assertValueCount(1)
        assertEquals(emptySet(), testObserver.values()[0])
    }

    @Test
    fun testGetFavorites() {
        val hotels = setOf("hotel1", "hotel2", "hotel3")
        HotelFavoritesCache.saveFavorites(context, hotels)
        val favorites = HotelFavoritesCache.getFavorites(context)
        assertEquals(hotels, favorites)
    }

    private fun assertSaveFavoriteId(hotelId: String, vararg cacheChangedSets: Set<String>) {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)
        HotelFavoritesCache.saveFavoriteId(context, hotelId)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId))
        testObserver.assertValueCount(cacheChangedSets.size)
        cacheChangedSets.forEachIndexed { i, cacheChangedSet ->
            assertEquals(cacheChangedSet, testObserver.values()[i])
        }
    }

    private fun assertRemoveFavoriteId(hotelIdsToSave: Set<String>, hotelIdToRemove: String, vararg cacheChangedSets: Set<String>) {
        val testObserver = TestObserver<Set<String>>()
        HotelFavoritesCache.cacheChangedSubject.subscribe(testObserver)
        HotelFavoritesCache.saveFavorites(context, hotelIdsToSave)
        HotelFavoritesCache.removeFavoriteId(context, hotelIdToRemove)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, hotelIdToRemove))
        testObserver.assertValueCount(cacheChangedSets.size + 1)
        assertEquals(hotelIdsToSave, testObserver.values()[0])
        cacheChangedSets.forEachIndexed { i, cacheChangedSet ->
            assertEquals(cacheChangedSet, testObserver.values()[i + 1])
        }
    }
}
