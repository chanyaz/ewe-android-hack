package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinSegmentSummaryViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinSegmentSummaryViewModel
    lateinit private var dateTime: DateTime

    val createAirlineWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.AirlineWidgetParams>()
    val createTimingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.TimingWidgetParams>()
    val createSeatingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.SeatingWidgetParams>()
    val updateTerminalGateSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.TerminalGateParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinSegmentSummaryViewModel(activity)
        dateTime = DateTime.now()
    }

    @Test
    fun testUpdateWidgetAirlineWidget() {
        sut.createAirlineWidgetSubject.subscribe(createAirlineWidgetSubscriber)
        createAirlineWidgetSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParams())

        createAirlineWidgetSubscriber.assertValueCount(1)
        createAirlineWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.AirlineWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "Operated by COMPASS AIRLINES"
        ))
    }

    @Test
    fun testUpdateWidgetTimingWidget() {
        sut.createTimingWidgetSubject.subscribe(createTimingWidgetSubscriber)
        createTimingWidgetSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParams())

        createTimingWidgetSubscriber.assertValueCount(1)
        createTimingWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TimingWidgetParams(
                LocaleBasedDateFormatUtils.dateTimeTohmma(dateTime).toLowerCase(),
                LocaleBasedDateFormatUtils.dateTimeTohmma(dateTime).toLowerCase(),
                "San Francisco (SFO)",
                "Las Vegas (LAS)"
        ))
    }

    @Test
    fun testUpdateWidgetSeatingWidget() {
        sut.createSeatingWidgetSubject.subscribe(createSeatingWidgetSubscriber)
        createSeatingWidgetSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParams())

        createSeatingWidgetSubscriber.assertValueCount(1)
        createSeatingWidgetSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SeatingWidgetParams(
                "No seats selected",
                "Economy / Coach",
                null
        ))
    }

    @Test
    fun testUpdateWidgetTerminalGateNullOrEmpty() {
        sut.updateTerminalGateSubject.subscribe(updateTerminalGateSubscriber)
        updateTerminalGateSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParams())

        updateTerminalGateSubscriber.assertValueCount(1)
        updateTerminalGateSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                null,
                null
        ))
    }

    @Test
    fun testUpdateWidgetTerminalNoGate() {
        sut.updateTerminalGateSubject.subscribe(updateTerminalGateSubscriber)
        updateTerminalGateSubscriber.assertNoValues()
        val summaryWidgetParams = getSummaryWidgetParams()
        summaryWidgetParams.departureTerminal = "3"
        summaryWidgetParams.departureGate = ""
        summaryWidgetParams.arrivalTerminal = "5"
        summaryWidgetParams.arrivalGate = null
        sut.updateWidget(summaryWidgetParams)

        updateTerminalGateSubscriber.assertValueCount(1)
        updateTerminalGateSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                "Terminal 3",
                "Terminal 5"
        ))
    }

    @Test
    fun testUpdateWidgetNoTerminalGate() {
        sut.updateTerminalGateSubject.subscribe(updateTerminalGateSubscriber)
        updateTerminalGateSubscriber.assertNoValues()
        val summaryWidgetParams = getSummaryWidgetParams()
        summaryWidgetParams.departureTerminal = ""
        summaryWidgetParams.departureGate = "3A"
        summaryWidgetParams.arrivalTerminal = null
        summaryWidgetParams.arrivalGate = "12"
        sut.updateWidget(summaryWidgetParams)

        updateTerminalGateSubscriber.assertValueCount(1)
        updateTerminalGateSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                "Gate 3A",
                "Gate 12"
        ))
    }

    @Test
    fun testUpdateWidgetTerminalGate() {
        sut.updateTerminalGateSubject.subscribe(updateTerminalGateSubscriber)
        updateTerminalGateSubscriber.assertNoValues()
        val summaryWidgetParams = getSummaryWidgetParams()
        summaryWidgetParams.departureTerminal = "1"
        summaryWidgetParams.departureGate = "3A"
        summaryWidgetParams.arrivalTerminal = "5"
        summaryWidgetParams.arrivalGate = "12"
        sut.updateWidget(summaryWidgetParams)

        updateTerminalGateSubscriber.assertValueCount(1)
        updateTerminalGateSubscriber.assertValue(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                "Terminal 1, Gate 3A",
                "Terminal 5, Gate 12"
        ))
    }

    @Test
    fun testGetTerminalGateString() {
        var terminalGateString = sut.getTerminalGateString(null, "")
        assertEquals(null, terminalGateString)

        terminalGateString = sut.getTerminalGateString(null, "3A")
        assertEquals("Gate 3A", terminalGateString)

        terminalGateString = sut.getTerminalGateString("5", "")
        assertEquals("Terminal 5", terminalGateString)

        terminalGateString = sut.getTerminalGateString("5", "3A")
        assertEquals("Terminal 5, Gate 3A", terminalGateString)
    }

    private fun getSummaryWidgetParams(): FlightItinSegmentSummaryViewModel.SummaryWidgetParams {
        return FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                dateTime,
                dateTime,
                "SFO",
                "San Francisco",
                "LAS",
                "Las Vegas",
                null,
                "",
                "",
                null,
                "No seats selected",
                "Economy / Coach",
                null
        )
    }
}