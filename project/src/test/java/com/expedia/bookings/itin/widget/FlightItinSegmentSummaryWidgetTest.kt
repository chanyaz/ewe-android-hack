package com.expedia.bookings.itin.widget

import android.content.Context
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
        assertEquals("21A, 23B, 25C", sut.seats.text )
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
}
