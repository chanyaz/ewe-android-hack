package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.RecordedRequest
import kotlin.test.assertEquals
import org.junit.Test

// TODO more coverage on the dispatching utilities
class DispatcherUtilitiesTest {

	@Test fun unEscapeUrl() {
		val urlString = "blah%20blahblah%20%20%30"
		val expected = "blah blahblah  %30"
		assertEquals(expected, unUrlEscape(urlString))
	}

	@Test fun emptyResponse() {
		val empty = makeEmptyResponse()
		assertEquals("HTTP/1.1 200 OK", empty.getStatus())
	}

	@Test fun response404() {
		val response404 = make404()
		assertEquals("HTTP/1.1 404 OK", response404.getStatus())
	}

    @Test fun parseRequestWorks() {
        val request = RecordedRequest("GET /hint/es/v2/ac/en_US/LAX?type=95&lob=Flights&  HTTP/1.1", null, null, 0, null, 0, null);
        parseRequest(request)
    }

}

