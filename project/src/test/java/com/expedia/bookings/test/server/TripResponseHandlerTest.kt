package com.expedia.bookings.test.server

import android.content.Context
import com.expedia.bookings.data.ServerError
import com.expedia.bookings.server.TripResponseHandler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripResponseHandlerTest {

    val sut = TripResponseHandler(getContext())
    lateinit var response: JSONObject

    @Test fun testHandleJsonHappy() {
        givenResponseWithTrip()

        val tripResponse = sut.handleJson(response)

        assertEquals(1, tripResponse.trips.size)
    }

    @Test fun testHandleJsonNoResponse() {
        givenEmptyResponse()

        val tripResponse = sut.handleJson(response)

        assertEquals(0, tripResponse.trips.size)
        assertTrue(tripResponse.hasErrors())
        assertEquals(ServerError.ErrorCode.TRIP_SERVICE_ERROR, tripResponse.errors[0].errorCode)
    }

    private fun givenEmptyResponse() {
        response = JSONObject()
    }

    private fun givenResponseWithTrip() {
        response = JSONObject()
        val trips = JSONArray()
        trips.put(JSONObject())
        response.put("responseData", trips)
    }

    private fun getContext(): Context {
        return Mockito.mock(Context::class.java)
    }
}
