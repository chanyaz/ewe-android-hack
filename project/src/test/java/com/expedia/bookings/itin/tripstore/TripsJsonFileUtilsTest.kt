package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.utils.TripsJsonFileUtils
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripsJsonFileUtilsTest {
    val data: String = getJsonStringFromMock("api/trips/hotel_trip_details.json", null)
    val dataTwo: String = getJsonStringFromMock("api/trips/hotel_trip_details_for_mocker.json", null)
    private val TRIPS_FILES_DIRECTORY = "TRIPS_FILES_DIRECTORY"
    private val TEST_FILENAME = "TEST_FILE"
    private val TEST_FILENAME_TWO = "TEST_FILE_2"
    lateinit var tripsDirectory: File
    lateinit var tripJsonUtils: TripsJsonFileUtils

    @Before
    fun setup() {
        tripsDirectory = File(TRIPS_FILES_DIRECTORY)
        tripsDirectory.mkdir()
        tripJsonUtils = TripsJsonFileUtils(tripsDirectory)
    }

    @After
    fun tearDown() {
        tripsDirectory.deleteRecursively()
    }

    @Test
    fun writeOnlyOneFile() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)

        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(TEST_FILENAME))

        assertEquals(1, tripFiles.size)
        assertTrue(tripFiles.contains(tripFile))
        assertEquals(data, tripFile.readText())
    }

    @Test
    fun writingIntoSameFileAgainOverwrites() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)

        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(TEST_FILENAME))

        assertEquals(1, tripFiles.size)
        assertTrue(tripFiles.contains(tripFile))
        assertEquals(data, tripFile.readText())
    }

    @Test
    fun writeEmptyFileName() {
        tripJsonUtils.writeTripToFile("", data)
        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(TEST_FILENAME))

        assertEquals(0, tripFiles.size)
        assertFalse(tripFiles.contains(tripFile))
    }

    @Test
    fun writeEmptyContent() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, "")
        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(TEST_FILENAME))

        assertEquals(0, tripFiles.size)
        assertFalse(tripFiles.contains(tripFile))
    }

    @Test
    fun writeEmptyFileNameContent() {
        tripJsonUtils.writeTripToFile("", "")
        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(TEST_FILENAME))

        assertEquals(0, tripFiles.size)
        assertFalse(tripFiles.contains(tripFile))
    }

    @Test
    fun writeLongFileName() {
        val SHARED_TRIP_FILE = "https://www.expedia.com/m/trips/shared/PviRCGFgfPM0NTy6zDg5-cCAHfv7oCCPfdYUMEMe6_aFzWOrjuX1wfhFeYfqL2zcCK38qjENrl4lgPJhKDirwYjr"
        tripJsonUtils.writeTripToFile(SHARED_TRIP_FILE, data)
        val tripFiles = tripsDirectory.listFiles()
        val tripFile = File(tripsDirectory, tripJsonUtils.hashString(SHARED_TRIP_FILE))

        assertEquals(1, tripFiles.size)
        assertTrue(tripFiles.contains(tripFile))
        assertEquals(data, tripFile.readText())
    }

    @Test
    fun writeFileNameNull() {
        tripJsonUtils.writeTripToFile(null, "test_content")
        val tripFiles = tripsDirectory.listFiles()
        assertEquals(0, tripFiles.size)
    }

    @Test
    fun writeContentNull() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, null)
        val tripFiles = tripsDirectory.listFiles()
        assertEquals(0, tripFiles.size)
    }

    @Test
    fun writeFileNAMEContentNull() {
        tripJsonUtils.writeTripToFile(null, null)
        val tripFiles = tripsDirectory.listFiles()
        assertEquals(0, tripFiles.size)
    }

    @Test
    fun readValidFile() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val readData = tripJsonUtils.readTripFromFile(TEST_FILENAME)
        assertNotNull(readData)
        assertEquals(data, readData)
    }

    @Test
    fun readTripsFromFile() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        tripJsonUtils.writeTripToFile(TEST_FILENAME_TWO, dataTwo)
        val readList = tripJsonUtils.readTripsFromFile()
        assertTrue(readList.contains(data))
        assertTrue(readList.contains(dataTwo))
    }

    @Test
    fun readTripsFromFileWithANull() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        tripJsonUtils.writeTripToFile(null, null)
        val readList = tripJsonUtils.readTripsFromFile()
        assertEquals(listOf(data), readList)
    }

    @Test
    fun readFileNotFound() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val readData = tripJsonUtils.readTripFromFile("INVALID_FILE")
        assertNull(readData)
    }

    @Test
    fun readEmptyFileName() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val readData = tripJsonUtils.readTripFromFile("")
        assertNull(readData)
    }

    @Test
    fun readFileNameNull() {
        tripJsonUtils.readTripFromFile(null)
        val tripFiles = tripsDirectory.listFiles()
        assertEquals(0, tripFiles.size)
    }

    @Test
    fun deleteTripFile() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val result = tripJsonUtils.deleteTripFile(TEST_FILENAME)
        assertTrue(result)
    }

    @Test
    fun deleteTripEmptyFileName() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val result = tripJsonUtils.deleteTripFile("")
        assertFalse(result)
    }

    @Test
    fun deleteTripInvalidFile() {
        tripJsonUtils.writeTripToFile(TEST_FILENAME, data)
        val result = tripJsonUtils.deleteTripFile("INVALID_FILE")
        assertFalse(result)
    }

    @Test
    fun deleteTripFilenameNull() {
        tripJsonUtils.deleteTripFile(null)
        val tripFiles = tripsDirectory.listFiles()
        assertEquals(0, tripFiles.size)
    }

    @Test
    fun deleteTripStore() {
        tripJsonUtils.writeTripToFile("FILE_1", data)
        tripJsonUtils.writeTripToFile("FILE_2", data)
        val tripFiles = tripsDirectory.listFiles()
        val tripFile1 = File(tripsDirectory, tripJsonUtils.hashString("FILE_1"))
        val tripFile2 = File(tripsDirectory, tripJsonUtils.hashString("FILE_2"))

        assertEquals(2, tripFiles.size)
        assertTrue(tripFiles.contains(tripFile1))
        assertTrue(tripFiles.contains(tripFile2))

        tripJsonUtils.deleteTripStore()
        assertTrue(tripsDirectory.exists())
        assertTrue(tripsDirectory.listFiles().isEmpty())
    }

    @Test
    fun testHashing() {
        val test1 = "ee7b032f-31e8-4a21-8cd8-3773bbf0ecf8"
        assertEquals("572208FDC43841EDA43E8AE46977833BAF45D5E8", tripJsonUtils.hashString(test1))

        val test2 = "https://www.expedia.com/m/trips/shared/PviRCGFgfPM0NTy6zDg5-cCAHfv7oCCPfdYUMEMe6_aFzWOrjuX1wfhFeYfqL2zcCK38qjENrl4lgPJhKDirwYjr"
        assertEquals("5246234684074EAC5C4B7C4E6AF68CE356AACDAA", tripJsonUtils.hashString(test2))

        val test3 = " "
        assertEquals("B858CB282617FB0956D960215C8E84D1CCF909C6", tripJsonUtils.hashString(test3))
    }
}
