package com.expedia.bookings.itin

import com.mobiata.mocke3.DispatcherSettingsKeys
import com.mobiata.mocke3.FileOpener
import com.mobiata.mocke3.TripsDispatcher
import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Test
import org.mockito.Mockito
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import kotlin.test.assertTrue

class TripsDispatcherTest {

    @Test
    fun testNoFilenamePassed() {
        val dispatcher = TripsDispatcher(Opener(), emptyMap())
        val response = dispatcher.dispatch(requestWithPath("test"))
        assertTrue(response.body.toString().contains("api/trips/tripfolders/tripfolders_happy_path_m1_hotel.json"))
    }

    @Test
    fun testHappyPath() {
        val dispatcher = TripsDispatcher(Opener(), mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to "tripfolders_m1_car"))
        val response = dispatcher.dispatch(requestWithPath("test"))
        assertTrue(response.body.toString().contains("api/trips/tripfolders/tripfolders_m1_car.json"))
    }

    private fun requestWithPath(path: String): RecordedRequest {
        val headers = Headers.of("user-agent", "ExpediaBookings/0.0.0 (EHad; Mobiata)")
        val testSocket = Mockito.mock(Socket::class.java)

        Mockito.`when`(testSocket.inetAddress).thenReturn(InetAddress.getByName("expedia.com"))
        Mockito.`when`(testSocket.localPort).thenReturn(80)

        return RecordedRequest("GET /$path HTTP/1.1", headers, null, 0, Buffer(), 0, testSocket)
    }

    private class Opener : FileOpener {
        override fun openFile(filename: String): InputStream = filename.byteInputStream()
    }
}
