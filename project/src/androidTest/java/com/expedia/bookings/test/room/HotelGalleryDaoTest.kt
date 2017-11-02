package com.expedia.bookings.test.room

import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.HotelDatabase
import com.expedia.bookings.hotel.dao.HotelGalleryDao
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class HotelGalleryDaoTest {
    private lateinit var testDao: HotelGalleryDao
    private lateinit var testDb: HotelDatabase

    private val expectedUrl = "/hotels/1000000/800000/796000/795934/795934_224_t.jpg"
    private val expectedDescription = "Room Images"
    private val expectedRoomCodeA = "Room A"
    private val expectedRoomCodeB = "Room B"
    private val expectedRoomCodeC = "Room C"

    private val infoDefault = PersistableHotelImageInfo(expectedUrl, expectedDescription)
    private val infoA = PersistableHotelImageInfo(expectedUrl, expectedDescription, expectedRoomCodeA, isRoomImage = true)
    private val infoB = PersistableHotelImageInfo(expectedUrl, expectedDescription, expectedRoomCodeB, isRoomImage = true)
    private val infoC = PersistableHotelImageInfo(expectedUrl, expectedDescription, expectedRoomCodeC, isRoomImage = true)


    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        testDb = Room.inMemoryDatabaseBuilder(context, HotelDatabase::class.java).build()
        testDao = testDb.hotelGalleryDao()
    }

    @Test
    fun testInsert() {
        testDao.insertAll(listOf(infoA))

        val list = testDao.findImagesForCode(expectedRoomCodeA)

        assertThat(list, notNullValue())
        assertThat(list!!.size, equalTo(1))
        assertThat(list[0].roomCode, equalTo(expectedRoomCodeA))
    }

    @Test
    fun testDeleteAllRoomLevel() {
        testDao.insertAll(listOf(infoA, infoB))

        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(1))
        assertThat(testDao.findImagesForCode(expectedRoomCodeB), hasSize(1))

        testDao.deleteAll(isRoomLevel = true)

        assertThat("deleteAll should remove ALL room related images, this ensures we don't grow our DB infinitely",
                testDao.findImagesForCode(expectedRoomCodeA), hasSize(0))
        assertThat("deleteAll should remove ALL room related images, this ensures we don't grow our DB infinitely",
                testDao.findImagesForCode(expectedRoomCodeB), hasSize(0))
    }

    @Test
    fun testDeleteAllRoomLevelDoesNotDeleteOverview() {
        testDao.insertAll(listOf(infoDefault, infoA))

        assertThat(testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(1))
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(1))

        testDao.deleteAll(isRoomLevel = true)
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(0))
        assertThat(testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(1))
    }

    @Test
    fun testDeleteAllOverviewDoesNotDeleteRoomLevel() {
        testDao.insertAll(listOf(infoDefault, infoA))

        assertThat(testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(1))
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(1))

        testDao.deleteAll(isRoomLevel = false)
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(1))
        assertThat(testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(0))
    }

    @Test
    fun testFindImageForValidCode() {
        testDao.insertAll(listOf(infoA))

        val list = testDao.findImagesForCode(expectedRoomCodeA)

        assertThat(list, hasSize(1))
        assertThat(list!![0].roomCode, equalTo(expectedRoomCodeA))
    }

    @Test
    fun testFindImageForInvalidCode() {
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(0))
        testDao.insertAll(listOf(infoB))
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(0))
    }

    @Test
    fun testReplaceDeletesOldRoomLevel() {
        testDao.insertAll(listOf(infoDefault, infoA, infoB))
        assertThat(testDao.findImagesForCode(expectedRoomCodeA), hasSize(1))
        assertThat(testDao.findImagesForCode(expectedRoomCodeB), hasSize(1))
        assertThat(testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(1))

        testDao.replaceImages(listOf(infoC), roomLevel = true)

        assertThat("Replacing room level images should clear all existing room level data",
                testDao.findImagesForCode(expectedRoomCodeA), hasSize(0))
        assertThat("Replacing room level images should clear all existing room level data",
                testDao.findImagesForCode(expectedRoomCodeB), hasSize(0))
        assertThat("Replacing room level images should NOT clear all existing default data",
                testDao.findImagesForCode(DEFAULT_HOTEL_GALLERY_CODE), hasSize(1))
        assertThat(testDao.findImagesForCode(expectedRoomCodeC), hasSize(1))
    }
}