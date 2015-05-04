package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.io.File
import kotlin.test.assertEquals
import org.junit.Test as test

// TODO more coverage on the dispatching utilities
class DispatcherUtilitiesTest {

	test fun unEscapeUrl() {
		val urlString = "blah%20blahblah%20%20%30"
		val expected = "blah blahblah  %30"
		assertEquals(expected, unUrlEscape(urlString))
	}

	test fun emptyResponse() {
		val empty = makeEmptyResponse()
		assertEquals("HTTP/1.1 200 OK", empty.getStatus())
	}

	test fun response404() {
		val response404 = make404()
		assertEquals("HTTP/1.1 404 OK", response404.getStatus())
	}

}

