package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.services.HotelShortlistServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFavoritesManagerTest {
    var shortlistServicesRule = ServicesRule(HotelShortlistServices::class.java)
        @Rule get

    private lateinit var favoritesManager: HotelFavoritesManager
    private val context = RuntimeEnvironment.application
    private val testHotelId = "Hotel123"

    @Before
    fun setup() {
        favoritesManager = HotelFavoritesManager(shortlistServicesRule.services!!)
    }

    @Test
    fun testSaveFavorite() {
        saveFavorite()
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testRemoveFavorite() {
        saveFavorite()
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        val testSubscriber = TestObserver<Unit>()
        favoritesManager.removeSuccessSubject.subscribe(testSubscriber)
        favoritesManager.removeFavorite(context, testHotelId)
        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testFetchFavorites() {
        val testSubscriber = TestObserver<HotelShortlistResponse<HotelShortlistItem>>()
        favoritesManager.fetchSuccessSubject.subscribe(testSubscriber)

        favoritesManager.fetchFavorites(context)
        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    private fun saveFavorite() {
        val params = getDummySearchParams()
        val testSubscriber = TestObserver<Unit>()
        favoritesManager.saveSuccessSubject.subscribe(testSubscriber)
        favoritesManager.saveFavorite(context, testHotelId, params)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    private fun getDummySearchParams(): HotelSearchParams {
        val destination = SuggestionV4()
        val builder = HotelSearchParams.Builder(0, 0)
                .destination(destination)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .adults(1)
        return builder.build() as HotelSearchParams
    }
}
