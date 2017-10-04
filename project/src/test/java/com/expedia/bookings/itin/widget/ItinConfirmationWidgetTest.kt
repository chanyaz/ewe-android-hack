package com.expedia.bookings.itin.widget

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinConfirmationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FlightClickAbleSpan
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinConfirmationWidgetTest {
    lateinit var context: Context
    lateinit var sut: ItinConfirmationWidget
    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_itin_confirmation, null) as ItinConfirmationWidget
        sut.viewModel = FlightItinConfirmationViewModel(context)
    }

    @Test
    fun testConfirmationStatus() {
        sut.viewModel.widgetConfirmationNumbersSubject.onNext("Confirmation")
        assertEquals("Confirmation", sut.confirmationNumbers.text.toString())
    }

    @Test
    fun testConfirmationNumberAndOmniture() {
        val confirmation = SpannableString("HY23T")
        val flightClickAble = FlightClickAbleSpan(context)
        confirmation.setSpan(flightClickAble, 0, confirmation.length, 0)
        sut.viewModel.widgetConfirmationNumbersSubject.onNext(confirmation)
        assertEquals("HY23T", sut.confirmationNumbers.text.toString())
        val textView = sut.findViewById<TextView>(R.id.confirmation_code_text_view)
        val text = textView.text as Spanned
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val spans = text.getSpans(0, text.length, FlightClickAbleSpan::class.java)
        spans[0].onClick(textView)
        OmnitureTestUtils.assertLinkTracked("App.Itinerary.Flight.CopyPNR", "App.Itinerary.Flight.CopyPNR", mockAnalyticsProvider)
    }
}
