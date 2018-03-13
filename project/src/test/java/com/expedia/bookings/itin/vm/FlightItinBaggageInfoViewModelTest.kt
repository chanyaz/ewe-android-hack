package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinWebviewInfoButtonViewModel
import com.expedia.bookings.itin.flight.details.FlightItinBagaggeInfoViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class FlightItinBaggageInfoViewModelTest {

    private lateinit var activity: Activity
    private lateinit var sut: FlightItinBagaggeInfoViewModel

    private val createBaggageInfoWidgetSubscriber = TestObserver<ItinWebviewInfoButtonViewModel.ItinWebviewInfoButtonWidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinBagaggeInfoViewModel(activity)
    }

    @Test
    fun testUpdateWebviewWidget() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.createWebviewButtonWidgetSubject.subscribe(createBaggageInfoWidgetSubscriber)
        createBaggageInfoWidgetSubscriber.assertNoValues()
        sut.updateWidgetWithBaggageInfoUrl(testItinCardData.baggageInfoUrl)
        createBaggageInfoWidgetSubscriber.assertValueCount(1)
        createBaggageInfoWidgetSubscriber.assertValue(ItinWebviewInfoButtonViewModel.ItinWebviewInfoButtonWidgetParams("Baggage information",
                R.drawable.ic_baggage_info_icon,
                R.color.app_primary,
                "https://www.expedia.com/Flights-BagFees?originapt=SFO&destinationapt=LAS&cabinclass=3&mktgcarrier=UA&opcarrier=&farebasis=GAA4AKEN&bookingclass=G&travelDate=2017-09-05&flightNumber=681"))
    }
}
