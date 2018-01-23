package com.expedia.bookings.itin.widget

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.vm.FlightItinModifyReservationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinModifyReservationWidgetTest {
    lateinit var activity: Activity
    lateinit var modifyReservationWidget: FlightItinModifyReservationWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        modifyReservationWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_modify_reservation_widget, null) as FlightItinModifyReservationWidget
        modifyReservationWidget.viewModel = FlightItinModifyReservationViewModel()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testViewVisibility() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        val params = FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webChangePathURL, true, webCancelPathURL, true, "(217)-546-7860")
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, true)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, true)
        assertEquals(modifyReservationWidget.cancelLearnMoreText.visibility, View.GONE)
        assertEquals(modifyReservationWidget.changeLearnMoreText.visibility, View.GONE)

        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, "", false, "(217)-546-7860"))
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.cancelLearnMoreText.visibility, View.VISIBLE)
        assertEquals(modifyReservationWidget.changeLearnMoreText.visibility, View.VISIBLE)

        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", true, "", true, "(217)-546-7860"))
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.cancelLearnMoreText.visibility, View.VISIBLE)
        assertEquals(modifyReservationWidget.changeLearnMoreText.visibility, View.VISIBLE)

        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webChangePathURL, false, webCancelPathURL, false, "(217)-546-7860"))
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.cancelLearnMoreText.visibility, View.VISIBLE)
        assertEquals(modifyReservationWidget.changeLearnMoreText.visibility, View.VISIBLE)
    }

    @Test
    fun testOmnitureForCancelFlight() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        val params = FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webChangePathURL, true, webCancelPathURL, true, "(217)-546-7860")
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        modifyReservationWidget.cancelReservationButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Cancel", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForChangeFlight() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        val params = FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webChangePathURL, true, webCancelPathURL, true, "(217)-546-7860")
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        modifyReservationWidget.changeReservationButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Change", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForChangeLearnMore() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, webChangePathURL, true, "(217)-546-7860"))
        modifyReservationWidget.changeLearnMoreText.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Change.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun testOminitureForCancelLearnMore() {
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webCancelPathURL, true, "", false, "(217)-546-7860"))
        modifyReservationWidget.cancelLearnMoreText.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Cancel.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun testChangeLearnMoreClick() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        val notChangeableText = "This reservation cannot be changed but you can contact customer support to explore your options."
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, webChangePathURL, true, "(217)-546-7860"))
        modifyReservationWidget.changeLearnMoreText.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(notChangeableText, dialogContentView.text.toString())
    }

    @Test
    fun testCancelLearnMoreClick() {
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        val notCancellableText = "This reservation cannot be canceled but you can contact customer support to explore your options."
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webCancelPathURL, true, "", false, "(217)-546-7860"))
        modifyReservationWidget.cancelLearnMoreText.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val dialogContentView = alertDialog.findViewById<TextView>(R.id.dialog_text_content)
        assertEquals(true, alertDialog.isShowing)
        assertEquals(notCancellableText, dialogContentView.text.toString())
    }

    @Test
    fun testChangeAndCancelLearnMoreClick() {
        val notChangeableOrCancellableText = "This reservation cannot be changed or canceled but you can contact customer support to explore your options."
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, "", false, "(217)-546-7860"))
        modifyReservationWidget.changeLearnMoreText.performClick()
        var alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        var dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        val customerSupportButton = alertDialog.findViewById<View>(R.id.dialog_customer_support)
        assertEquals(true, alertDialog.isShowing)
        assertEquals(notChangeableOrCancellableText, dialogContentView.text.toString())

        modifyReservationWidget.cancelLearnMoreText.performClick()
        alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(notChangeableOrCancellableText, dialogContentView.text.toString())

        customerSupportButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Call.Expedia", mockAnalyticsProvider)
    }
}
