package com.expedia.bookings.test

import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailTicketDeliveryEntryViewModelTest {

    @Test
    fun testDeliveryOptions() {
        val viewModel = RailTicketDeliveryEntryViewModel(RuntimeEnvironment.application)
        val testSubscriberTicketDeliveryByPostOptions = TestSubscriber.create<List<RailCreateTripResponse.RailTicketDeliveryOption>>()
        val testSubscriberDeliveryByMailSupported = TestSubscriber.create<Boolean>()
        viewModel.ticketDeliveryByPostOptions.subscribe(testSubscriberTicketDeliveryByPostOptions)
        viewModel.deliveryByMailSupported.subscribe(testSubscriberDeliveryByMailSupported)

        var ticketDeliveryOptionsAll = ArrayList<RailCreateTripResponse.RailTicketDeliveryOption>()

        val option1 = RailCreateTripResponse.RailTicketDeliveryOption()
        option1.ticketDeliveryCountryCodeList = listOf("GB")
        ticketDeliveryOptionsAll.add(option1)

        val option2 = RailCreateTripResponse.RailTicketDeliveryOption()
        option2.ticketDeliveryCountryCodeList = listOf("IN")
        ticketDeliveryOptionsAll.add(option2)

        viewModel.ticketDeliveryOptions.onNext(ticketDeliveryOptionsAll)
        testSubscriberTicketDeliveryByPostOptions.assertValueCount(1)
        assertEquals(1, testSubscriberTicketDeliveryByPostOptions.onNextEvents[0].size)
        testSubscriberDeliveryByMailSupported.assertValueCount(1)
        assertTrue(testSubscriberDeliveryByMailSupported.onNextEvents[0])

        option1.ticketDeliveryCountryCodeList = emptyList()
        option2.ticketDeliveryCountryCodeList = emptyList()

        viewModel.ticketDeliveryOptions.onNext(ticketDeliveryOptionsAll)
        testSubscriberTicketDeliveryByPostOptions.assertValueCount(2)
        assertEquals(0, testSubscriberTicketDeliveryByPostOptions.onNextEvents[1].size)
        testSubscriberDeliveryByMailSupported.assertValueCount(2)
        assertFalse(testSubscriberDeliveryByMailSupported.onNextEvents[1])

    }
}