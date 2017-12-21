package com.expedia.bookings.unit;

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
import com.expedia.bookings.data.MIDItinDetailsResponse
import com.expedia.bookings.data.PackageItinDetailsResponse
import com.expedia.bookings.services.ItinTripServices
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.expedia.bookings.testrule.ServicesRule

class ItinTripServicesTest {
	var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.immediate(), "../mocked/templates")
		@Rule get

	@Test
	fun testTripDetailsNotAvailable() {
		val itinDetailsResponse = ItinDetailsResponse()
		assertNull(itinDetailsResponse.getResponseDataForItin())
	}

	@Test
	fun testTripDetailsAvailable() {
		val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("flight_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("53a6459c-822c-4425-9e14-3eea43f38a97", testObserver.onNextEvents[0].getResponseDataForItin()?.tripId)
	}

	@Test
	fun testHotelTripDetailsAvailable() {
		val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("hotel_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("fb24d134-adbd-44f6-9904-48cfb33bbd50", testObserver.onNextEvents[0].getResponseDataForItin()?.tripId)
		assertTrue(testObserver.onNextEvents[0] is HotelItinDetailsResponse)
	}

	@Test
	fun testPackageTripDetailsAvailable() {
		val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("package_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("4d0385c3-9d0e-42ca-b7de-103d423f583c", testObserver.onNextEvents[0].getResponseDataForItin()?.tripId)
		assertTrue(testObserver.onNextEvents[0] is PackageItinDetailsResponse)
	}

	@Test
	fun testMIDTripDetailsAvailable() {
		val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("mid_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("ff88710c-97a1-4469-bf7f-66ff66430dd7", testObserver.onNextEvents[0].getResponseDataForItin()?.tripId)
		assertTrue(testObserver.onNextEvents[0] is MIDItinDetailsResponse)
	}
}
