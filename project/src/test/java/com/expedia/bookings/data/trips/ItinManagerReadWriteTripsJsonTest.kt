package com.expedia.bookings.data.trips

import com.expedia.bookings.itin.tripstore.utils.TripsJsonFileUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinManagerReadWriteTripsJsonTest {

    private lateinit var testFileDirectory: File
    private lateinit var tripJsonFileUtils: TripsJsonFileUtils
    private val itinManager = ItineraryManager.getInstance()
    private val USER_TRIP_FILENAME = "USER_TRIP_FILENAME"
    private val SHARED_TRIP_FILENAME = "SHARED_TRIP_FILENAME"
    private val mockJSONString = "random_hotel_trip_details_json_here"
    private val mockJSONObject = Mockito.mock(JSONObject::class.java)
    private val guestJSONString = "{\"responseData\":{\"isGuest\":true}}"
    private val sharedJSONString = "{\"responseData\":{\"isShared\":true}}"
    private val faultyJSONString = "{}"
    private lateinit var validJson: JSONObject
    private lateinit var syncTask: ItineraryManager.SyncTask
    private lateinit var userTripFile: File
    private lateinit var sharedTripFile: File

    @Before
    fun setup() {
        Mockito.`when`(mockJSONObject.toString()).thenReturn(mockJSONString)
        testFileDirectory = File("TRIPS_FILES_DIRECTORY")
        testFileDirectory.mkdir()
        tripJsonFileUtils = TripsJsonFileUtils(testFileDirectory)
        itinManager.setTripsJsonFileUtils(tripJsonFileUtils)
        syncTask = itinManager.SyncTask(null, null, null)
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
        validJson = makeJSON()
        syncTask.writeTripJsonResponseToFile(trip, validJson)

        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(sharedTripFile))
        assertFalse(files.contains(userTripFile))
        assertEquals(sharedJSONString, sharedTripFile.readText())
    }

    @Test
    fun writeTripJsonToFileFaultySharedTrip() {
        val trip = makeSharedTrip()
        validJson = makeJSON(true)
        syncTask.writeTripJsonResponseToFile(trip, validJson)

        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(sharedTripFile))
        assertFalse(files.contains(userTripFile))
        assertEquals(faultyJSONString, sharedTripFile.readText())
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

    @Test
    fun writeGuestTrip() {
        val trip = makeGuestTrip()
        validJson = makeJSON()
        syncTask.writeTripJsonResponseToFile(trip, validJson)
        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(userTripFile))
        assertFalse(files.contains(sharedTripFile))
        assertEquals(guestJSONString, userTripFile.readText())
    }

    @Test
    fun writeFaultyGuestTrip() {
        val trip = makeGuestTrip()
        validJson = makeJSON(true)
        syncTask.writeTripJsonResponseToFile(trip, validJson)
        val files = testFileDirectory.listFiles()
        assertTrue(files.contains(userTripFile))
        assertFalse(files.contains(sharedTripFile))
        assertEquals(faultyJSONString, userTripFile.readText())
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

    private fun makeGuestTrip(): Trip {
        val trip = Trip("email@email.com", "131323231")
        trip.setIsShared(false)
        trip.tripId = USER_TRIP_FILENAME
        return trip
    }

    private fun makeJSON(faulty: Boolean = false): JSONObject {
        return if (faulty) {
            JSONObject("{}")
        } else {
            JSONObject("{\"responseData\":{}}")
        }
    }
}
