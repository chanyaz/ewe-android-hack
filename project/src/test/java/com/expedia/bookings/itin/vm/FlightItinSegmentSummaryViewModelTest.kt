package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.utils.FormatUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightItinSegmentSummaryViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinSegmentSummaryViewModel

    val createAirlineWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.AirlineWidgetParams>()
    val createTimingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.TimingWidgetParams>()
    val createSeatingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.SeatingWidgetParams>()
    lateinit var context: Context

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinSegmentSummaryViewModel(activity)
        context = RuntimeEnvironment.application
    }

    @Test
    fun testUpdateWidget() {
        sut.createAirlineWidgetSubject.subscribe(createAirlineWidgetSubscriber)
        sut.createTimingWidgetSubject.subscribe(createTimingWidgetSubscriber)
        sut.createSeatingWidgetSubject.subscribe(createSeatingWidgetSubscriber)

        createAirlineWidgetSubscriber.assertNoValues()
        createTimingWidgetSubscriber.assertNoValues()
        createSeatingWidgetSubscriber.assertNoValues()

        val testItinCardData = ItinCardDataFlightBuilder().build()
        val segment = testItinCardData.flightLeg.getSegment(0)
        val departureTime = segment.originWaypoint.bestSearchDateTime
        val arrivalTime = segment.destinationWaypoint.bestSearchDateTime

        sut.updateWidget(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                testItinCardData.flightLeg.airlineLogoURL,
                FormatUtils.formatFlightNumber(testItinCardData.flightLeg.getSegment(0), activity),
                segment.operatingFlightCode.mAirlineName,
                departureTime,
                arrivalTime,
                segment.originWaypoint.airport.mAirportCode,
                segment.originWaypoint.airport.mCity,
                segment.destinationWaypoint.airport.mAirportCode,
                segment.destinationWaypoint.airport.mCity,
                context.getString(R.string.select_seat_prompt),
                segment.cabinCode,
                null


        ))

        createAirlineWidgetSubscriber.assertValueCount(1)
        createAirlineWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.AirlineWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "Operated by COMPASS AIRLINES"
        ))
        createTimingWidgetSubscriber.assertValueCount(1)
        createTimingWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TimingWidgetParams(
                LocaleBasedDateFormatUtils.dateTimeTohmma(departureTime).toLowerCase(),
                LocaleBasedDateFormatUtils.dateTimeTohmma(arrivalTime).toLowerCase(),
                "San Francisco (SFO)",
                "Las Vegas (LAS)"
        ))
        createSeatingWidgetSubscriber.assertValueCount(1)
        createSeatingWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SeatingWidgetParams(
                "No seats selected",
                "Economy / Coach",
                null
        ))
    }
}