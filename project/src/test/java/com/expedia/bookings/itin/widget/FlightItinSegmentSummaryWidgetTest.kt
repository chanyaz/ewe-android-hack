package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinSegmentSummaryViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinSegmentSummaryWidgetTest {
    lateinit var context: Context
    lateinit var sut: FlightItinSegmentSummaryWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_flight_segment_summary_widget, null) as FlightItinSegmentSummaryWidget
        sut.viewModel = FlightItinSegmentSummaryViewModel(context)
    }

    @Test
    fun testAirlineWidget() {
        sut.viewModel.createAirlineWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.AirlineWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "Operated by COMPASS AIRLINES"))
        assertEquals("United Airlines 681", sut.airlineNameAndNumber.text.toString())
        assertEquals(View.VISIBLE, sut.operatedByAirlines.visibility)
        assertEquals("Operated by COMPASS AIRLINES", sut.operatedByAirlines.text.toString())
    }

    @Test
    fun testAirlineWidgetWithoutOperatedBy() {
        sut.viewModel.createAirlineWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.AirlineWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                null))
        assertEquals("United Airlines 681", sut.airlineNameAndNumber.text.toString())
        assertEquals(View.GONE, sut.operatedByAirlines.visibility)
    }

    @Test
    fun testTimingWidget() {
        sut.viewModel.createTimingWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.TimingWidgetParams(
                "8:49pm",
                "9:49pm",
                "San Francisco (SFO)",
                "Las Vegas (LAS)"
        ))
        assertEquals("8:49pm", sut.departureTime.text.toString())
        assertEquals("9:49pm", sut.arrivalTime.text.toString())
        assertEquals("San Francisco (SFO)", sut.departureAirport.text.toString())
        assertEquals("Las Vegas (LAS)", sut.arrivalAirport.text.toString())
    }

    @Test
    fun testTerminalGateNullOrEmpty() {
        sut.viewModel.updateTerminalGateSubject.onNext(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                null,
                ""
        ))
        assertEquals(View.GONE, sut.departureTerminalGate.visibility)
        assertEquals(View.GONE, sut.arrivalTerminalGate.visibility)
    }

    @Test
    fun testTerminalGate() {
        sut.viewModel.updateTerminalGateSubject.onNext(FlightItinSegmentSummaryViewModel.TerminalGateParams(
                "Terminal 3, Gate 12A",
                "Terminal 12"
        ))
        assertEquals(View.VISIBLE, sut.departureTerminalGate.visibility)
        assertEquals(View.VISIBLE, sut.arrivalTerminalGate.visibility)
        assertEquals("Terminal 3, Gate 12A", sut.departureTerminalGate.text.toString())
        assertEquals("Terminal 12", sut.arrivalTerminalGate.text.toString())
    }

    fun testSeatingWidget() {
        sut.viewModel.createSeatingWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.SeatingWidgetParams(
                "21A, 23B, 25C",
                "• Economy / Coach",
                "Confirm or change seats with airline"

        ))
        assertEquals("21A, 23B, 25C", sut.seats.text)
        assertEquals("• Economy / Coach", sut.cabin.text)
        assertEquals(View.VISIBLE, sut.seatConfirmation.visibility)
        assertEquals("Confirm or change seats with airline", sut.seatConfirmation.text)
    }

    @Test
    fun testSeatingWidgetwithNoSeats() {
        sut.viewModel.createSeatingWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.SeatingWidgetParams(
                "No seats selected",
                "• Economy / Coach",
                null

        ))
        assertEquals("No seats selected", sut.seats.text)
        assertEquals("• Economy / Coach", sut.cabin.text)
        assertEquals(View.GONE, sut.seatConfirmation.visibility)
        assertEquals("", sut.seatConfirmation.text)
    }

    @Test
    fun testRedEyeWidgetWithoutRedEye() {
        sut.viewModel.createRedEyeWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.RedEyeParams(
                null,
                null,
                null
        ))
        assertEquals(View.GONE, sut.redEyeDays.visibility)
        assertEquals(View.GONE, sut.arrivalRedEye.visibility)
        assertEquals(View.GONE, sut.departureRedEye.visibility)
    }

    @Test
    fun testRedEyeWidgetWithRedEye() {
        sut.viewModel.createRedEyeWidgetSubject.onNext(FlightItinSegmentSummaryViewModel.RedEyeParams(
                "Wed, Oct 18",
                "Arrives on Thu, Oct 19",
                "+1"
        ))
        assertEquals(View.VISIBLE, sut.redEyeDays.visibility)
        assertEquals(View.VISIBLE, sut.arrivalRedEye.visibility)
        assertEquals(View.VISIBLE, sut.departureRedEye.visibility)
        assertEquals("Wed, Oct 18", sut.departureRedEye.text)
        assertEquals("Arrives on Thu, Oct 19", sut.arrivalRedEye.text)
        assertEquals("+1", sut.redEyeDays.text)
    }

    @Test
    fun testFlightCancelled() {
        val indicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_cancelled)
        sut.viewModel.updateFlightStatusSubject.onNext(FlightItinSegmentSummaryViewModel.FlightStatsParams(
                R.drawable.flight_status_indicator_error_background,
                indicatorText,
                indicatorText,
                R.color.itin_status_indicator_error,
                null,
                null
        ))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorContainer.visibility)
        assertTrue(sut.flightStatusIndicatorContainer.background == ContextCompat.getDrawable(context, R.drawable.flight_status_indicator_error_background))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorText.visibility)
        assertEquals("Cancelled", sut.flightStatusIndicatorText.text.toString())
        assertEquals("Cancelled", sut.flightStatusIndicatorText.contentDescription.toString())

        assertEquals(View.GONE, sut.newDepartureDetailsContainer.visibility)
        assertEquals(View.GONE, sut.newArrivalDetailsContainer.visibility)
    }

    @Test
    fun testFlightOnTime() {
        val indicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_on_time)
        sut.viewModel.updateFlightStatusSubject.onNext(FlightItinSegmentSummaryViewModel.FlightStatsParams(
                R.drawable.flight_status_indicator_success_background,
                indicatorText,
                indicatorText,
                R.color.itin_status_indicator_success,
                null,
                null
        ))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorContainer.visibility)
        assertTrue(sut.flightStatusIndicatorContainer.background == ContextCompat.getDrawable(context, R.drawable.flight_status_indicator_success_background))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorText.visibility)
        assertEquals("On time", sut.flightStatusIndicatorText.text.toString())
        assertEquals("On time", sut.flightStatusIndicatorText.contentDescription.toString())

        assertEquals(View.GONE, sut.newDepartureDetailsContainer.visibility)
        assertEquals(View.GONE, sut.newArrivalDetailsContainer.visibility)
    }

    @Test
    fun testFlightEarlyDeparture() {
        val indicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_early_departure)
        sut.viewModel.updateFlightStatusSubject.onNext(FlightItinSegmentSummaryViewModel.FlightStatsParams(
                R.drawable.flight_status_indicator_success_background,
                indicatorText,
                indicatorText,
                R.color.itin_status_indicator_success,
                "9:45am",
                "10:45am"
        ))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorContainer.visibility)
        assertTrue(sut.flightStatusIndicatorContainer.background == ContextCompat.getDrawable(context, R.drawable.flight_status_indicator_success_background))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorText.visibility)
        assertEquals("Early departure", sut.flightStatusIndicatorText.text.toString())
        assertEquals("Early departure", sut.flightStatusIndicatorText.contentDescription.toString())

        assertEquals(View.VISIBLE, sut.newDepartureDetailsContainer.visibility)
        assertEquals(View.VISIBLE, sut.newArrivalDetailsContainer.visibility)
        assertEquals("9:45am", sut.newDepartureTimeText.text.toString())
        assertEquals("10:45am", sut.newArrivalTimeText.text.toString())
        assertEquals(ContextCompat.getColor(context, R.color.itin_status_indicator_success), sut.newDepartureTimeText.currentTextColor)
        assertEquals(ContextCompat.getColor(context, R.color.itin_status_indicator_success), sut.newArrivalTimeText.currentTextColor)
    }

    @Test
    fun testFlightDelayed() {
        sut.viewModel.updateFlightStatusSubject.onNext(FlightItinSegmentSummaryViewModel.FlightStatsParams(
                R.drawable.flight_status_indicator_error_background,
                "Delayed by 30m",
                "Delayed by 30m",
                R.color.itin_status_indicator_error,
                "10:45am",
                "11:45am"
        ))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorContainer.visibility)
        assertTrue(sut.flightStatusIndicatorContainer.background == ContextCompat.getDrawable(context, R.drawable.flight_status_indicator_error_background))

        assertEquals(View.VISIBLE, sut.flightStatusIndicatorText.visibility)
        assertEquals("Delayed by 30m", sut.flightStatusIndicatorText.text.toString())
        assertEquals("Delayed by 30m", sut.flightStatusIndicatorText.contentDescription.toString())

        assertEquals(View.VISIBLE, sut.newDepartureDetailsContainer.visibility)
        assertEquals(View.VISIBLE, sut.newArrivalDetailsContainer.visibility)
        assertEquals("10:45am", sut.newDepartureTimeText.text.toString())
        assertEquals("11:45am", sut.newArrivalTimeText.text.toString())
        assertEquals(ContextCompat.getColor(context, R.color.itin_status_indicator_error), sut.newDepartureTimeText.currentTextColor)
        assertEquals(ContextCompat.getColor(context, R.color.itin_status_indicator_error), sut.newArrivalTimeText.currentTextColor)
    }
}
