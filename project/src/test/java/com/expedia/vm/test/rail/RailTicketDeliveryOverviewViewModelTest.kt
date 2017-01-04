package com.expedia.vm.test.rail

import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.rail.widget.TicketDeliveryMethod
import com.expedia.vm.rail.RailTicketDeliveryOverviewViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTicketDeliveryOverviewViewModelTest {

    val context = RuntimeEnvironment.application
    val ticketDeliveryOverviewVM = RailTicketDeliveryOverviewViewModel(context)

    @Test
    fun testPickupAtStation() {
        val testDeliveryIconSubscriber = TestSubscriber<Int>()
        val testDeliveryTitleSubscriber = TestSubscriber<String>()
        ticketDeliveryOverviewVM.iconObservable.subscribe(testDeliveryIconSubscriber)
        ticketDeliveryOverviewVM.titleObservable.subscribe(testDeliveryTitleSubscriber)

        ticketDeliveryOverviewVM.ticketDeliverySelectedObserver.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
        assertEquals(R.drawable.ticket_delivery_cko_station, testDeliveryIconSubscriber.onNextEvents[0])
        assertEquals("Pick-up at station", testDeliveryTitleSubscriber.onNextEvents[0])
    }

    @Test
    fun testDeliveryByMail() {
        val testDeliveryIconSubscriber = TestSubscriber<Int>()
        val testDeliveryTitleSubscriber = TestSubscriber<String>()
        ticketDeliveryOverviewVM.iconObservable.subscribe(testDeliveryIconSubscriber)
        ticketDeliveryOverviewVM.titleObservable.subscribe(testDeliveryTitleSubscriber)

        ticketDeliveryOverviewVM.ticketDeliverySelectedObserver.onNext(TicketDeliveryMethod.DELIVER_BY_MAIL)
        assertEquals(R.drawable.ticket_delivery_cko_mail, testDeliveryIconSubscriber.onNextEvents[0])
        assertEquals("Delivery by mail", testDeliveryTitleSubscriber.onNextEvents[0])
    }
}
