package com.expedia.bookings.itin.tripstore

import android.content.Context
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import org.json.JSONArray
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class JsonToFoldersUtilTest {
    private val context: Context = RuntimeEnvironment.application
    private val fileUtils = Ui.getApplication(context).tripComponent().tripFolderJsonFileUtils()
    private val utilToTest = Ui.getApplication(context).tripComponent().jsonToFolderUtil()

    @Before
    fun setup() {
        fileUtils.deleteAllFiles()
    }

    @After
    fun tearDown() {
        fileUtils.deleteAllFiles()
    }

    @Test
    fun noFolderExists() {
        val folders = utilToTest.getTripFolders()
        assertTrue(folders.isEmpty())
    }

    @Test
    fun corruptedFolder() {
        fileUtils.writeToFile("TEST_FILE_1", "Corrupted Content")
        val folders = utilToTest.getTripFolders()
        assertTrue(folders.isEmpty())
    }

    @Test
    fun oneFolderExists() {
        val arrayOfFolders = JSONArray(getJsonStringFromMock("api/trips/tripfolders/tripfolders_happy_path_m1_hotel.json", null))
        fileUtils.writeToFile("2515dc80-7aa5-4ddb-ad66-1c381d36989d", arrayOfFolders[0].toString())
        val folders = utilToTest.getTripFolders()
        assertTrue(folders.isNotEmpty())
        assertTrue(folders.size == 1)
        assertEquals("2515dc80-7aa5-4ddb-ad66-1c381d36989d", folders[0].tripFolderId)
    }

    @Test
    fun multipleFoldersExist() {
        val arrayOfFolders = JSONArray(getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null))
        val tripFolderIds = listOf(
                "8a246ebb-ef3d-43cc-aa9e-0bede99e38bd",
                "c99f4136-f468-42e8-b387-4a88f9909cec",
                "2515dc80-7aa5-4ddb-ad66-1c381d36989d",
                "70cd722e-20c5-4d81-8bb1-cbdee92b2ea5"
        )
        fileUtils.writeToFile(tripFolderIds[0], arrayOfFolders[0].toString())
        fileUtils.writeToFile(tripFolderIds[1], arrayOfFolders[1].toString())
        fileUtils.writeToFile(tripFolderIds[2], arrayOfFolders[2].toString())
        fileUtils.writeToFile(tripFolderIds[3], arrayOfFolders[3].toString())
        val folders = utilToTest.getTripFolders()
        assertTrue(folders.isNotEmpty())
        assertTrue(folders.size == 4)
        val actualFolderIds = listOf(
                folders[0].tripFolderId,
                folders[1].tripFolderId,
                folders[2].tripFolderId,
                folders[3].tripFolderId
        )
        assertTrue(actualFolderIds.containsAll(tripFolderIds))
    }
}
