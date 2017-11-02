package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelMediaStorageConverterTest {
    private val testConverter = HotelMediaStorageConverter()

    private val expectedUrl = "/hotels/1000000/800000/796000/795934/795934_224_t.jpg"
    private val expectedDescription = "Room Images"
    private val expectedRoomCode = "Room A"

    @Test
    fun testToPersistableWithRoomCode() {
        val media = HotelMedia(expectedUrl, expectedDescription)

        val persistable = testConverter.toPersistable(media, expectedRoomCode)

        assertEquals(expectedUrl, persistable.url)
        assertEquals(expectedDescription, persistable.displayText)
        assertEquals(expectedRoomCode, persistable.roomCode)
        assertEquals(true, persistable.isRoomImage,
                "FAILURE: Any item that does not use the DEFAULT_HOTEL_GALLERY_CODE must be labeled as a roomImage")
    }

    @Test
    fun testToPersistableDefaultCode() {
        val media = HotelMedia(expectedUrl, expectedDescription)

        val persistable = testConverter.toPersistable(media, DEFAULT_HOTEL_GALLERY_CODE)

        assertEquals(expectedUrl, persistable.url)
        assertEquals(expectedDescription, persistable.displayText)
        assertEquals(DEFAULT_HOTEL_GALLERY_CODE, persistable.roomCode)
        assertFalse(persistable.isRoomImage)
    }

    @Test
    fun testToPersistableList() {
        val mediaList = ArrayList<HotelMedia>()
        mediaList.add(HotelMedia(expectedUrl, expectedDescription))
        mediaList.add(HotelMedia(expectedUrl, expectedDescription))

        val persistableList = testConverter.toPersistableList(mediaList, expectedRoomCode)

        assertEquals(mediaList.size, persistableList.size)
        val persistableSampleItem = persistableList[0]
        assertEquals(expectedUrl, persistableSampleItem.url)
        assertEquals(expectedDescription, persistableSampleItem.displayText)
        assertEquals(expectedRoomCode, persistableSampleItem.roomCode)
        assertTrue(persistableSampleItem.isRoomImage)
    }

    @Test
    fun testToMediaList() {
        val persistableList = ArrayList<PersistableHotelImageInfo>()
        persistableList.add(PersistableHotelImageInfo(expectedUrl, expectedDescription))
        persistableList.add(PersistableHotelImageInfo(expectedUrl, expectedDescription))

        val mediaList = testConverter.toHotelMediaList(persistableList)
        assertEquals(mediaList.size, persistableList.size)
        val mediaSampleItem = mediaList[0]
        assertEquals(expectedUrl, mediaSampleItem.originalUrl)
        assertEquals(expectedDescription, mediaSampleItem.description)
    }
}