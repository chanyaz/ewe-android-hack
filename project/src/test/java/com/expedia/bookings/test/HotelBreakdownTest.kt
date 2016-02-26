package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.vm.Breakdown
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import rx.observers.TestSubscriber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue

class HotelBreakdownTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private var vm: HotelBreakDownViewModel by Delegates.notNull()
    private var hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel by Delegates.notNull()
    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")
        Mockito.`when`(context.getString(Matchers.anyInt())).thenReturn("")
        hotelCheckoutSummaryViewModel = HotelCheckoutSummaryViewModel(context)
        vm = HotelBreakDownViewModel(context, hotelCheckoutSummaryViewModel)
    }

    @Test
    fun verifyBreakdown() {
        givenHappyCreateTripResponse()

        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }
        val testSubscriber = TestSubscriber<List<Breakdown>>()
        val expected = arrayListOf<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        hotelCheckoutSummaryViewModel.newRateObserver.onNext(createTripResponse.newHotelProductResponse)
        expected.add(arrayListOf(Breakdown("", "$99.00", false, false), Breakdown("3/22/2013", "$99.00", true, false), Breakdown("", "$16.81", false, false), Breakdown("", "$135.81", false, false)))

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onCompleted()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }

    private fun givenHappyCreateTripResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
    }
}
