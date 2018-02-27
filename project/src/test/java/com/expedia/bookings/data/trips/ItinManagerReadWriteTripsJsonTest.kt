package com.expedia.bookings.data.trips

import com.expedia.bookings.features.Feature
import com.expedia.bookings.itin.tripstore.utils.TripsJsonFileUtils
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinManagerReadWriteTripsJsonTest {

    private lateinit var testFileDirectory: File
    private lateinit var tripJsonFileUtils: TripsJsonFileUtils
    private val itinManager = ItineraryManager.getInstance()
    private val USER_TRIP_FILENAME = "USER_TRIP_FILENAME"
    private val SHARED_TRIP_FILENAME = "SHARED_TRIP_FILENAME"
    private val mockJSONString = "random_hotel_trip_details_json_here"
    private val mockJSONObject = Mockito.mock(JSONObject::class.java)
    private lateinit var syncTask: ItineraryManager.SyncTask
    private lateinit var userTripFile: File
    private lateinit var sharedTripFile: File

    @Before
    fun setup() {
        Mockito.`when`(mockJSONObject.toString()).thenReturn(mockJSONString)
        testFileDirectory = File("TRIPS_FILES_DIRECTORY")
        testFileDirectory.mkdir()
        tripJsonFileUtils = TripsJsonFileUtils(testFileDirectory)
        itinManager.setItineraryManagerStoreTripsJsonFeature(StoreJsonFeature())
        itinManager.setTripsJsonFileUtils(tripJsonFileUtils)
        syncTask = itinManager.SyncTask(null, null)
        sharedTripFile = File(testFileDirectory, tripJsonFileUtils.hashString(SHARED_TRIP_FILENAME))
        userTripFile = File(testFileDirectory, tripJsonFileUtils.hashString(USER_TRIP_FILENAME))
    }

    @After
    fun tearDown() {
        testFileDirectory.deleteRecursively()
    }

    @Test
    fun writeTripJsonToFileSharedTrip() {
        val trip = makeSharedTrip()

        syncTask.writeTripJsonResponseToFile(trip, mockJSONObject)

        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(sharedTripFile))
        assertFalse(files.contains(userTripFile))
        assertEquals(mockJSONString, sharedTripFile.readText())
    }

    @Test
    fun writeTripJsonToFileUserTrip() {
        val trip = makeUserTrip()

        syncTask.writeTripJsonResponseToFile(trip, mockJSONObject)

        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(userTripFile))
        assertFalse(files.contains(sharedTripFile))
        assertEquals(mockJSONString, userTripFile.readText())
    }

    @Test
    fun deleteSharedTrip() {
        val trip = makeSharedTrip()
        syncTask.writeTripJsonResponseToFile(trip, mockJSONObject)
        var files = testFileDirectory.listFiles()
        assertTrue(files.contains(sharedTripFile))
        assertFalse(files.contains(userTripFile))

        syncTask.deleteTripJsonFromFile(trip)

        files = testFileDirectory.listFiles()
        assertFalse(files.contains(sharedTripFile))
        assertFalse(files.contains(userTripFile))
    }

    @Test
    fun deleteUserTrip() {
        val trip = makeUserTrip()
        syncTask.writeTripJsonResponseToFile(trip, mockJSONObject)
        var files = testFileDirectory.listFiles()
        assertTrue(files.contains(userTripFile))
        assertFalse(files.contains(sharedTripFile))

        syncTask.deleteTripJsonFromFile(trip)

        files = testFileDirectory.listFiles()
        assertFalse(files.contains(userTripFile))
        assertFalse(files.contains(sharedTripFile))
    }

    private fun makeUserTrip(): Trip {
        val trip = Trip()
        trip.tripId = USER_TRIP_FILENAME
        trip.setIsShared(false)
        return trip
    }

    private fun makeSharedTrip(): Trip {
        val trip = Trip()
        trip.tripId = null
        trip.setIsShared(true)
        trip.shareInfo.sharableDetailsUrl = SHARED_TRIP_FILENAME
        return trip
    }

    private class StoreJsonFeature : Feature {
        override val name: String
            get() = "StoreJsonFeature"

        override fun enabled() = true
    }
}
