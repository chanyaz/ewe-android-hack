package com.expedia.vm.test.rail

import android.content.Context
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailCheckoutOverviewViewModelTest {

    @Test
    fun testCheckInAndCheckOutDate() {
        val viewmodel = RailCheckoutOverviewViewModel(getContext())

        val params = RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .origin(RailSearchRequestMock.origin("Origin"))
                .destination(RailSearchRequestMock.destination("Destination")).build() as RailSearchRequest

        val checkInAndCheckOutDateTestSubscriber = TestSubscriber<Pair<String, String>>()
        val checkInWithoutCheckoutDateTestSubscriber = TestSubscriber<String>()

        viewmodel.checkInAndCheckOutDate.subscribe(checkInAndCheckOutDateTestSubscriber)
        viewmodel.checkInWithoutCheckoutDate.subscribe(checkInWithoutCheckoutDateTestSubscriber)

        viewmodel.params.onNext(params)

        checkInAndCheckOutDateTestSubscriber.assertValueCount(1)
        checkInWithoutCheckoutDateTestSubscriber.assertValueCount(0)

        assertEquals("1989-09-06", checkInAndCheckOutDateTestSubscriber.onNextEvents.first().first)
        assertEquals("2021-09-06", checkInAndCheckOutDateTestSubscriber.onNextEvents.first().second)
    }

    @Test
    fun testCheckInWithoutCheckOutDate() {
        val viewmodel = RailCheckoutOverviewViewModel(getContext())

        val params = RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .origin(RailSearchRequestMock.origin("Origin"))
                .destination(RailSearchRequestMock.destination("Destination")).build() as RailSearchRequest

        val checkInAndCheckOutDateTestSubscriber = TestSubscriber<Pair<String, String>>()
        val checkInWithoutCheckoutDateTestSubscriber = TestSubscriber<String>()

        viewmodel.checkInAndCheckOutDate.subscribe(checkInAndCheckOutDateTestSubscriber)
        viewmodel.checkInWithoutCheckoutDate.subscribe(checkInWithoutCheckoutDateTestSubscriber)

        viewmodel.params.onNext(params)

        checkInAndCheckOutDateTestSubscriber.assertValueCount(0)
        checkInWithoutCheckoutDateTestSubscriber.assertValueCount(1)

        assertEquals("1989-09-06", checkInWithoutCheckoutDateTestSubscriber.onNextEvents.first())
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}