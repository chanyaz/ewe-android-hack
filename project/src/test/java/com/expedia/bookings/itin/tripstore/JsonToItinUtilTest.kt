package com.expedia.bookings.itin.tripstore

import android.content.Context
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.JsonToItinUtil
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

@RunWith(RobolectricRunner::class)
class JsonToItinUtilTest {
    private val context: Context = RuntimeEnvironment.application
    private val TEST_FILENAME = "TEST_FILE"
    private val fileUtils = Ui.getApplication(context).appComponent().tripJsonFileUtils()

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
        val itin = JsonToItinUtil.getItin(context, TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun nullItinId() {
        val itin = JsonToItinUtil.getItin(context, null)
        assertNull(itin)
    }

    @Test
    fun validItinIdInvalidJson() {
        fileUtils.writeTripToFile(TEST_FILENAME, "blah blah")
        val itin = JsonToItinUtil.getItin(context, TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun validItinIdValidJsonInvalidItin() {
        val errorData: String = getJsonStringFromMock("api/trips/error_bad_request_trip_response.json", null)
        fileUtils.writeTripToFile(TEST_FILENAME, errorData)
        val itin = JsonToItinUtil.getItin(context, TEST_FILENAME)
        assertNull(itin)
    }

    @Test
    fun validItinIdValidJsonValidItin() {
        val mockData: String = getJsonStringFromMock("api/trips/hotel_trip_details.json", null)
        val mockObject = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")?.itin
        fileUtils.writeTripToFile(TEST_FILENAME, mockData)
        val itin = JsonToItinUtil.getItin(context, TEST_FILENAME)
        assertEquals(mockObject, itin)
    }
}
