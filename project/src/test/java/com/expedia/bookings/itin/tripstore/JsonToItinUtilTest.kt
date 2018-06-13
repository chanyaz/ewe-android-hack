package com.expedia.bookings.itin.tripstore

import android.content.Context
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import com.mobiata.mocke3.mockObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class JsonToItinUtilTest {
    private val context: Context = RuntimeEnvironment.application
    private val TEST_FILENAME = "TEST_FILE"
    private val fileUtils = Ui.getApplication(context).appComponent().tripJsonFileUtils()
    private val jsonUtils = Ui.getApplication(context).appComponent().jsonUtilProvider()

    @Before
    fun setup() {
        fileUtils.deleteTripStore()
    }

    @After
    fun tearDown() {
        fileUtils.deleteTripStore()
    }

    @Test
    fun noJsonFileExists() {
        val itin = jsonUtils.getItin(TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun nullItinId() {
        val itin = jsonUtils.getItin(null)
        assertNull(itin)
    }

    @Test
    fun validItinIdInvalidJson() {
        fileUtils.writeTripToFile(TEST_FILENAME, "blah blah")
        val itin = jsonUtils.getItin(TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun validItinIdValidJsonInvalidItin() {
        val errorData: String = getJsonStringFromMock("api/trips/error_bad_request_trip_response.json", null)
        fileUtils.writeTripToFile(TEST_FILENAME, errorData)
        val itin = jsonUtils.getItin(TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun validItinIdValidJsonValidItin() {
        val mockData: String = getJsonStringFromMock("api/trips/hotel_trip_details.json", null)
        val mockObject = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")?.itin
        fileUtils.writeTripToFile(TEST_FILENAME, mockData)
        val itin = jsonUtils.getItin(TEST_FILENAME)
        assertEquals(mockObject, itin)
    }

    @Test
    fun getItinListHappy() {
        val firstMockData: String = getJsonStringFromMock("api/trips/hotel_trip_details_for_mocker.json", null)
        val firstMockObject = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin
        val secondMockData: String = getJsonStringFromMock("api/trips/car_trip_details_happy.json", null)
        val secondMockObject = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_happy.json")?.itin
        fileUtils.writeTripToFile("Mock1", firstMockData)
        fileUtils.writeTripToFile("Mock2", secondMockData)
        val itinList = jsonUtils.getItinList()
        assertTrue(itinList.contains(firstMockObject))
        assertTrue(itinList.contains(secondMockObject))
    }

    @Test
    fun getItinListOneValidOneInvalid() {
        val secondMockData: String = getJsonStringFromMock("api/trips/car_trip_details_happy.json", null)
        val secondMockObject = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_happy.json")?.itin
        fileUtils.writeTripToFile("Mock1", "Yo")
        fileUtils.writeTripToFile("Mock2", secondMockData)
        val itinList = jsonUtils.getItinList()
        assertEquals(listOf(secondMockObject), itinList)
    }

    @Test
    fun getItinListInvalidItin() {
        fileUtils.writeTripToFile("Mock1", "Yo")
        val itinList = jsonUtils.getItinList()
        assertEquals(emptyList(), itinList)
    }
}
