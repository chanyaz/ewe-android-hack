package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.dao.HotelGalleryDao
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.Scheduler
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelGalleryManagerTest {
    private lateinit var testManager: TestGalleryManager
    private val testDao = Mockito.mock(HotelGalleryDao::class.java)

    private val listCaptor = ArgumentCaptor<List<PersistableHotelImageInfo>>()

    @Before
    fun createDb() {
        val context = RuntimeEnvironment.application
        testManager = TestGalleryManager(context, testDao)
    }

    @Test
    fun testSaveImagesNoRooms() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_info_sold_out.json")

        testManager.saveHotelOfferMedia(response)

        Mockito.verify(testDao, Mockito.times(1))
                .replaceImages(capture(listCaptor), Mockito.anyBoolean())
        val overviewCapture = listCaptor.value
        val overviewSampleItem = overviewCapture[0]

        assertEquals(3, overviewCapture.size)
        assertEquals(DEFAULT_HOTEL_GALLERY_CODE, overviewSampleItem.roomCode)
        assertFalse(overviewSampleItem.isRoomImage)
    }

    @Test
    fun testSaveImagesWithRooms() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_happy_offer.json")

        testManager.saveHotelOfferMedia(response)

        Mockito.verify(testDao, Mockito.times(2))
                .replaceImages(capture(listCaptor), Mockito.anyBoolean())

        val overviewCapture = listCaptor.allValues[0]
        val overviewSampleItem = overviewCapture[0]
        assertEquals(2, overviewCapture.size)
        assertEquals(DEFAULT_HOTEL_GALLERY_CODE, overviewSampleItem.roomCode)
        assertFalse(overviewSampleItem.isRoomImage)

        val roomCapture = listCaptor.allValues[1]
        val roomSampleItem = roomCapture[0]
        assertEquals(5, roomCapture.size)
        assertEquals("200674447", roomSampleItem.roomCode)
        assertTrue(roomSampleItem.isRoomImage)
    }

    @Test
    fun testFetchMedia() {
        val expectedUrl = "/hotels/1000000/800000/796000/795934/795934_224_t.jpg"
        val expectedList = ArrayList<PersistableHotelImageInfo>()
        expectedList.add(PersistableHotelImageInfo(expectedUrl, "description"))

        val testSubscriber = TestSubscriber.create<ArrayList<HotelMedia>>()
        Mockito.`when`(testDao.findImagesForCode(Mockito.anyString())).thenReturn(expectedList)
        testManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE, testSubscriber)

        val mediaList = testSubscriber.onNextEvents[0]
        val sampleMediaItem = mediaList[0]
        assertEquals(expectedUrl, sampleMediaItem.originalUrl)
        assertEquals("description", sampleMediaItem.description)
    }

    @Test
    fun testEmptyRemovesOld() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_info_sold_out.json")
        testManager.saveHotelOfferMedia(response)

        Mockito.verify(testDao, Mockito.times(1))
                .replaceImages(capture(listCaptor), Mockito.anyBoolean())

        response.photos = null
        testManager.saveHotelOfferMedia(response)
        Mockito.verify(testDao, Mockito.times(1)).deleteAll(Mockito.anyBoolean())
    }

    private fun loadOfferInfo(resourcePath: String) : HotelOffersResponse {
        val resourceReader = JSONResourceReader(resourcePath)
        return resourceReader.constructUsingGson(HotelOffersResponse::class.java)
    }

    private class TestGalleryManager(context: Context, private val testDao: HotelGalleryDao) :
            HotelGalleryManager(context) {
        override val galleryDao: HotelGalleryDao
            get() = testDao

        override val workScheduler: Scheduler
            get() = Schedulers.immediate()
    }

    private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}