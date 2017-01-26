package com.expedia.bookings.unit;

import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
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
		assertNull(itinDetailsResponse.responseData)
	}

	@Test
	fun testTripDetailsAvailable() {
		val testObserver: TestSubscriber<ItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("flight_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("53a6459c-822c-4425-9e14-3eea43f38a97", testObserver.onNextEvents[0].responseData?.tripId)
	}

	@Test
	fun testHotelTripDetailsAvailable() {
		val testObserver: TestSubscriber<ItinDetailsResponse> = TestSubscriber.create()
		serviceRule.services!!.getTripDetails("hotel_trip_details", testObserver)

		testObserver.awaitTerminalEvent()
		testObserver.assertCompleted()
		testObserver.assertValueCount(1)
		assertEquals("fb24d134-adbd-44f6-9904-48cfb33bbd50", testObserver.onNextEvents[0].responseData?.tripId)
		assertTrue(testObserver.onNextEvents[0] is HotelItinDetailsResponse)
	}

}
