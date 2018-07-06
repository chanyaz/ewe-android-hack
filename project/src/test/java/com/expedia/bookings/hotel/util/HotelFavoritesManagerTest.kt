package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import com.expedia.bookings.services.HotelShortlistServices
import com.expedia.bookings.services.HotelShortlistServicesInterface
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.lang.reflect.Type
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFavoritesManagerTest {
    var shortlistServicesRule = ServicesRule(HotelShortlistServices::class.java)
        @Rule get

    private val context = RuntimeEnvironment.application
    private val testHotelId = "Hotel123"

    private lateinit var favoritesManager: HotelFavoritesManager

    private lateinit var mockService: MockHotelShortlistServices
    private lateinit var favoritesManagerWithWaitExecute: HotelFavoritesManager
    private val waitExecuteSaveSuccessTestSubscriber = TestObserver<Unit>()
    private val waitExecuteRemoveSuccessTestSubscriber = TestObserver<Unit>()

    @Before
    fun setup() {
        favoritesManager = HotelFavoritesManager(shortlistServicesRule.services!!)

        mockService = MockHotelShortlistServices()
        favoritesManagerWithWaitExecute = HotelFavoritesManager(mockService)

        favoritesManagerWithWaitExecute.saveSuccessSubject.subscribe(waitExecuteSaveSuccessTestSubscriber)
        favoritesManagerWithWaitExecute.removeSuccessSubject.subscribe(waitExecuteRemoveSuccessTestSubscriber)

        HotelFavoritesCache.clearFavorites(context)
    }

    @Test
    fun testSaveFavorite() {
        saveFavorite()
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testSaveFavoriteWithShortlistItem() {
        val metadata = ShortlistItemMetadata().apply {
            hotelId = testHotelId
            chkIn = "20180617"
            chkOut = "20180616"
            roomConfiguration = "1"
        }
        val shortlistItem = HotelShortlistItem().apply {
            shortlistItem = ShortlistItem()
            shortlistItem!!.metaData = metadata
        }
        val testSubscriber = TestObserver<Unit>()
        favoritesManager.saveSuccessSubject.subscribe(testSubscriber)
        favoritesManager.saveFavorite(context, shortlistItem)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
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

    @Test
    fun testFormatRoomConfig() {
        val formattedRoomConfig = favoritesManager.formatRoomConfig(1, listOf(3, 4))
        assertEquals("1|3-4", formattedRoomConfig)
    }

    @Test
    fun testFormatRoomConfigNoChildren() {
        val formattedRoomConfig = favoritesManager.formatRoomConfig(5, emptyList())
        assertEquals("5", formattedRoomConfig)
    }

    @Test
    fun testFormatRoomConfigNoAdult() {
        val formattedRoomConfig = favoritesManager.formatRoomConfig(0, listOf(18, 21))
        assertEquals("0|18-21", formattedRoomConfig)
    }

    @Test
    fun testFormatRoomConfigNegativeNumber() {
        val formattedRoomConfig = favoritesManager.formatRoomConfig(-6, listOf(-1, -10, -100))
        assertEquals("-6|-1--10--100", formattedRoomConfig)
    }

    @Test
    fun testFormatRoomConfigNoAdultAndChildren() {
        val formattedRoomConfig = favoritesManager.formatRoomConfig(0, emptyList())
        assertEquals("0", formattedRoomConfig)
    }

    @Test
    fun testRequestQueue() {
        val hotelShortlistItem = getDummyHotelShortlistItem(testHotelId)

        favoritesManagerWithWaitExecute.saveFavorite(context, hotelShortlistItem)
        assertQueueState(0, 0, 1, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)
        assertQueueState(0, 0, 1, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeSave()
        assertQueueState(0, 0, 0, 1)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeRemove()
        assertQueueState(0, 1, 0, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testRequestQueueMultipleHotelId() {
        val hotelId1 = "hotelId1"
        val hotelId2 = "hotelId2"

        val hotelSearchParams = getDummySearchParams()

        favoritesManagerWithWaitExecute.saveFavorite(context, hotelId1, hotelSearchParams)
        favoritesManagerWithWaitExecute.saveFavorite(context, hotelId2, hotelSearchParams)
        favoritesManagerWithWaitExecute.removeFavorite(context, hotelId2)
        favoritesManagerWithWaitExecute.removeFavorite(context, hotelId1)
        favoritesManagerWithWaitExecute.saveFavorite(context, hotelId2, hotelSearchParams)
        favoritesManagerWithWaitExecute.saveFavorite(context, hotelId1, hotelSearchParams)

        assertQueueState(0, 0, 2, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, hotelId1))
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, hotelId2))

        mockService.executeSave()
        assertQueueState(0, 0, 1, 1)

        mockService.executeSave()
        assertQueueState(0, 0, 0, 2)

        mockService.executeRemove()
        assertQueueState(0, 0, 1, 1)

        mockService.executeSave()
        assertQueueState(1, 0, 0, 1)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId1))

        mockService.executeRemove()
        assertQueueState(1, 0, 1, 0)

        mockService.executeSave()
        assertQueueState(2, 0, 0, 0)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, hotelId2))
    }

    @Test
    fun testRequestQueueSaveSameHotelIdMultipleTimes() {
        val hotelSearchParams = getDummySearchParams()
        val shortlistItem = getDummyHotelShortlistItem(testHotelId)

        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)
        favoritesManagerWithWaitExecute.saveFavorite(context, shortlistItem)

        assertQueueState(0, 0, 1, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeSave()
        mockService.executeSave()

        assertQueueState(1, 0, 0, 0)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        favoritesManagerWithWaitExecute.saveFavorite(context, shortlistItem)
        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)

        assertQueueState(1, 0, 1, 0)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeSave()
        mockService.executeSave()

        assertQueueState(2, 0, 0, 0)
        assertTrue(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testRequestQueueRemoveSameHotelIdMultipleTimes() {
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)

        assertQueueState(0, 0, 0, 1)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeRemove()
        mockService.executeRemove()

        assertQueueState(0, 1, 0, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)

        assertQueueState(0, 1, 0, 1)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.executeRemove()
        mockService.executeRemove()

        assertQueueState(0, 2, 0, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testRequestQueueFailedRequestQueueNextRequest() {
        val hotelSearchParams = getDummySearchParams()

        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)
        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)

        assertQueueState(0, 0, 0, 1)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.failRemove()
        assertQueueState(0, 0, 1, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        mockService.failSave()
        assertQueueState(0, 0, 0, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))

        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)

        mockService.executeSave()
        mockService.failRemove()

        assertQueueState(0, 0, 0, 0)
        assertFalse(HotelFavoritesCache.isFavoriteHotel(context, testHotelId))
    }

    @Test
    fun testRequestQueueFetchDoesntEffectSaveDelete() {
        val hotelSearchParams = getDummySearchParams()
        val testObserver = TestObserver<HotelShortlistResponse<HotelShortlistItem>>()

        favoritesManagerWithWaitExecute.fetchSuccessSubject.subscribe(testObserver)

        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)
        favoritesManagerWithWaitExecute.fetchFavorites(context)
        favoritesManagerWithWaitExecute.saveFavorite(context, testHotelId, hotelSearchParams)
        favoritesManagerWithWaitExecute.removeFavorite(context, testHotelId)

        assertQueueState(0, 0, 1, 0)
        assertEquals(1, mockService.fetchQueue.size)
        testObserver.assertEmpty()

        mockService.executeSave()
        assertQueueState(0, 0, 0, 1)
        assertEquals(1, mockService.fetchQueue.size)
        testObserver.assertEmpty()

        mockService.executeRemove()
        assertQueueState(0, 0, 1, 0)
        assertEquals(1, mockService.fetchQueue.size)
        testObserver.assertEmpty()

        mockService.executeSave()
        assertQueueState(0, 0, 0, 1)
        assertEquals(1, mockService.fetchQueue.size)
        testObserver.assertEmpty()

        mockService.executeFetch()
        assertQueueState(0, 0, 0, 1)
        assertEquals(0, mockService.fetchQueue.size)
        testObserver.assertValueCount(1)

        mockService.executeRemove()
        assertQueueState(0, 1, 0, 0)
        assertEquals(0, mockService.fetchQueue.size)
        testObserver.assertValueCount(1)
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
                .adults(2)
                .children(listOf(3, 4))
        return builder.build() as HotelSearchParams
    }

    private fun getDummyHotelShortlistItem(hotelId: String): HotelShortlistItem {
        return HotelShortlistItem().apply {
            this.shortlistItem = ShortlistItem().apply {
                this.metaData = ShortlistItemMetadata().apply {
                    this.hotelId = hotelId
                    chkIn = LocalDate.now().toString("yyyyMMdd")
                    chkOut = LocalDate.now().toString("yyyyMMdd")
                    roomConfiguration = "2|3-4"
                }
            }
        }
    }

    private fun assertQueueState(saveSuccessCount: Int, removeSuccessCount: Int, saveQueueSize: Int, removeQueueSize: Int) {
        waitExecuteSaveSuccessTestSubscriber.assertValueCount(saveSuccessCount)
        waitExecuteRemoveSuccessTestSubscriber.assertValueCount(removeSuccessCount)
        assertEquals(saveQueueSize, mockService.saveQueue.size)
        assertEquals(removeQueueSize, mockService.removeQueue.size)
    }

    private class MockHotelShortlistServices : HotelShortlistServicesInterface {
        data class SaveQueueData(val hotelId: String, val metadata: ShortlistItemMetadata, val observer: Observer<HotelShortlistResponse<ShortlistItem>>)

        var fetchQueue = LinkedList<Observer<HotelShortlistResponse<HotelShortlistItem>>>()
        var saveQueue = LinkedList<SaveQueueData>()
        var removeQueue = LinkedList<Observer<ResponseBody>>()

        private val fetchSubject = TestObserver<HotelShortlistResponse<HotelShortlistItem>>()
        private val saveSubject = TestObserver<HotelShortlistResponse<ShortlistItem>>()
        private val removeSubject = TestObserver<ResponseBody>()

        override fun fetchFavoriteHotels(observer: Observer<HotelShortlistResponse<HotelShortlistItem>>): Disposable {
            fetchQueue.add(observer)
            return fetchSubject
        }

        override fun saveFavoriteHotel(hotelId: String, metadata: ShortlistItemMetadata, observer: Observer<HotelShortlistResponse<ShortlistItem>>): Disposable {
            saveQueue.add(SaveQueueData(hotelId, metadata, observer))
            return saveSubject
        }

        override fun removeFavoriteHotel(hotelId: String, observer: Observer<ResponseBody>): Disposable {
            removeQueue.add(observer)
            return removeSubject
        }

        fun executeFetch() {
            if (fetchQueue.isNotEmpty()) {
                val observer = fetchQueue.pollFirst()
                val type: Type = object : TypeToken<HotelShortlistResponse<HotelShortlistItem>>() {}.type
                val json = getJsonStringFromMock("api/hotelshortlist/hotelShortlistFetchResponse.json", null)

                val fetchResponse = Gson().fromJson<HotelShortlistResponse<HotelShortlistItem>>(json, type)

                observer.onNext(fetchResponse)
                fetchSubject.onNext(fetchResponse)
            }
        }

        fun executeSave() {
            if (saveQueue.isNotEmpty()) {
                val saveData = saveQueue.pollFirst()

                val type: Type = object : TypeToken<HotelShortlistResponse<ShortlistItem>>() {}.type
                val json = getJsonStringFromMock("api/hotelshortlist/hotelShortlistSaveResponse.json", null)
                val saveResponse = Gson().fromJson<HotelShortlistResponse<ShortlistItem>>(json, type)

                saveResponse.results[0].items[0].itemId = saveData.hotelId
                saveResponse.results[0].items[0].metaData = saveData.metadata
                saveData.observer.onNext(saveResponse)
                saveSubject.onNext(saveResponse)
            }
        }

        fun failSave() {
            if (saveQueue.isNotEmpty()) {
                val saveData = saveQueue.pollFirst()
                val error = Error()
                saveData.observer.onError(error)
                saveSubject.onError(error)
            }
        }

        fun executeRemove() {
            if (removeQueue.isNotEmpty()) {
                val observer = removeQueue.pollFirst()

                val responseBody = ResponseBody.create(null, "")

                observer.onNext(responseBody)
                removeSubject.onNext(responseBody)
            }
        }

        fun failRemove() {
            if (removeQueue.isNotEmpty()) {
                val observer = removeQueue.pollFirst()
                val error = Error()
                observer.onError(error)
            }
        }
    }
}
