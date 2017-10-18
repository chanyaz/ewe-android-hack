package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinSegmentSummaryViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinSegmentSummaryViewModel
    lateinit private var dateTime: DateTime
    lateinit private var frozenTime: DateTime

    val createAirlineWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.AirlineWidgetParams>()
    val createTimingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.TimingWidgetParams>()
    val createSeatingWidgetSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.SeatingWidgetParams>()
    val updateTerminalGateSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.TerminalGateParams>()
    var createRedEyeSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.RedEyeParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinSegmentSummaryViewModel(activity)
        dateTime = DateTime.now()
        frozenTime = DateTime(2017, 10, 18, 12, 0)
    }

    @Test
    fun testUpdateWidgetRedEyeWidgetWithoutRedEye() {
        sut.createRedEyeWidgetSubject.subscribe(createRedEyeSubscriber)
        createRedEyeSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParams())

        createRedEyeSubscriber.assertValueCount(1)
        createRedEyeSubscriber.assertValues(FlightItinSegmentSummaryViewModel.RedEyeParams(
                null,
                null,
                null
        ))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testUpdateWidgetRedEyeWidgetWithRedEye() {
        sut.createRedEyeWidgetSubject.subscribe(createRedEyeSubscriber)
        createRedEyeSubscriber.assertNoValues()
        sut.updateWidget(getSummaryWidgetParamsWithRedEye())

        createRedEyeSubscriber.assertValueCount(1)
        createRedEyeSubscriber.assertValues(FlightItinSegmentSummaryViewModel.RedEyeParams(
                "Wed, Oct 18",
                "Arrives on Thu, Oct 19",
                "+1"
        ))
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
                null,
                null
        )
    }
    private fun getSummaryWidgetParamsWithRedEye(): FlightItinSegmentSummaryViewModel.SummaryWidgetParams {
        return FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                frozenTime,
                frozenTime.plusDays(1),
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
                null,
                "+1"
        )
    }
}