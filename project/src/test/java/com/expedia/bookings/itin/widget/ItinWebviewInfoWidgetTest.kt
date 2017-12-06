package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.vm.FlightItinBagaggeInfoViewModel
import com.expedia.bookings.itin.vm.ItinWebviewInfoButtonViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import kotlinx.android.synthetic.main.widget_itin_webview_button.view.itin_webview_button_container
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinWebviewInfoWidgetTest {

    lateinit var context: Context
    lateinit var sut: ItinWebviewInfoWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_itin_webview_button, null) as ItinWebviewInfoWidget
        sut.viewModel = FlightItinBagaggeInfoViewModel(context)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun baggageInfoTextAndClick() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.createWebviewButtonWidgetSubject.onNext(ItinWebviewInfoButtonViewModel
                .ItinWebviewInfoButtonWidgetParams("Baggage information",
                        R.drawable.ic_baggage_info_icon,
                        R.color.app_primary,
                        testItinCardData.baggageInfoUrl))
        val expectedTextColor = ContextCompat.getColor(context, R.color.app_primary)
        assertEquals(View.VISIBLE, sut.visibility)
        assertEquals("Baggage information", sut.buttonText.text.toString())
        assertEquals(expectedTextColor, sut.buttonText.currentTextColor)
        assertTrue(sut.itin_webview_button_container.hasOnClickListeners())
        sut.itin_webview_button_container.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Baggage.Info", mockAnalyticsProvider)
    }

    @Test
    fun noWebviewURL() {
        sut.viewModel.createWebviewButtonWidgetSubject.onNext(ItinWebviewInfoButtonViewModel
                .ItinWebviewInfoButtonWidgetParams("Baggage information",
                        R.drawable.ic_baggage_info_icon,
                        R.color.app_primary,
                        null))
        assertEquals(View.GONE, sut.visibility)
    }

}