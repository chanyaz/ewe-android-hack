//TODO Reenable class when these tests can run


//package com.mobiata.mocke3
//
//import okhttp3.mockwebserver.RecordedRequest
//import org.junit.Test
//import java.net.Socket
//import kotlin.test.assertEquals
//
//// TODO more coverage on the dispatching utilities
//class DispatcherUtilitiesTest {
//
//	@Test fun unEscapeUrl() {
//		val urlString = "blah%20blahblah%20%20%30"
//		val expected = "blah blahblah  %30"
//		assertEquals(expected, unUrlEscape(urlString))
//	}
//
//	@Test fun emptyResponse() {
//		val empty = makeEmptyResponse()
//		assertEquals("HTTP/1.1 200 OK", empty.status)
//	}
//
//	@Test fun response404() {
//		val response404 = make404()
//		assertEquals("HTTP/1.1 404 Client Error", response404.status)
//	}
//
//	@Test fun parseRequestWorks() {
//		val socket = Socket("expedia.com", 80)
//		val request = RecordedRequest("GET /hint/es/v2/ac/en_US/LAX?type=95&lob=Flights&  HTTP/1.1", null, null, 0, null, 0, socket)
//		parseHttpRequest(request)
//	}
//
//}
//
