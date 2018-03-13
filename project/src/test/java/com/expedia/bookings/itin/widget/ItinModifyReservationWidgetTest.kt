package com.expedia.bookings.itin.widget

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.flight.manageBooking.FlightItinModifyReservationViewModel
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinModifyReservationViewModel
import com.expedia.bookings.itin.common.ItinModifyReservationWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinModifyReservationWidgetTest {
    lateinit var activity: Activity
    lateinit var modifyReservationWidget: ItinModifyReservationWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        modifyReservationWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_modify_reservation_widget, null) as ItinModifyReservationWidget
        modifyReservationWidget.viewModel = FlightItinModifyReservationViewModel(activity)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testViewVisibility() {
        assertEquals(View.VISIBLE, modifyReservationWidget.cancelLearnMoreText.visibility)
        assertEquals(false, modifyReservationWidget.cancelReservationButton.isEnabled)
        modifyReservationWidget.viewModel.cancelReservationSubject.onNext(Unit)
        assertEquals(View.GONE, modifyReservationWidget.cancelLearnMoreText.visibility)
        assertEquals(View.VISIBLE, modifyReservationWidget.cancelReservationButton.visibility)
        assertEquals(true, modifyReservationWidget.cancelReservationButton.isEnabled)

        assertEquals(View.VISIBLE, modifyReservationWidget.changeLearnMoreText.visibility)
        assertEquals(false, modifyReservationWidget.changeReservationButton.isEnabled)
        modifyReservationWidget.viewModel.changeReservationSubject.onNext(Unit)
        assertEquals(View.GONE, modifyReservationWidget.changeLearnMoreText.visibility)
        assertEquals(View.VISIBLE, modifyReservationWidget.changeReservationButton.visibility)
        assertEquals(true, modifyReservationWidget.changeReservationButton.isEnabled)
    }

    @Test
    fun testOmnitureForCancelFlight() {
        modifyReservationWidget.viewModel.cancelReservationSubject.onNext(Unit)
        modifyReservationWidget.cancelReservationButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Cancel", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForChangeFlight() {
        modifyReservationWidget.viewModel.changeReservationSubject.onNext(Unit)
        modifyReservationWidget.changeReservationButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Change", mockAnalyticsProvider)
    }

    @Test
    fun testChangeHotelWebView() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val shadow = Shadows.shadowOf(activity)
        modifyReservationWidget.viewModel = HotelItinModifyReservationViewModel(activity)
        modifyReservationWidget.viewModel.itinCardSubject.onNext(itinCardDataHotel)
        modifyReservationWidget.viewModel.changeReservationSubject.onNext(Unit)
        modifyReservationWidget.changeReservationButton.performClick()
        val intent = shadow.peekNextStartedActivityForResult()
        assertEquals(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE, intent.requestCode)
        assertEquals(itinCardDataHotel.tripNumber, intent.intent.getStringExtra(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER))
    }

    @Test
    fun testCancelHotelWebView() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val shadow = Shadows.shadowOf(activity)
        modifyReservationWidget.viewModel = HotelItinModifyReservationViewModel(activity)
        modifyReservationWidget.viewModel.itinCardSubject.onNext(itinCardDataHotel)
        modifyReservationWidget.viewModel.cancelReservationSubject.onNext(Unit)
        modifyReservationWidget.cancelReservationButton.performClick()
        val intent = shadow.peekNextStartedActivityForResult()
        assertEquals(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE, intent.requestCode)
        assertEquals(itinCardDataHotel.tripNumber, intent.intent.getStringExtra(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER))
    }

    @Test
    fun testOmnitureForChangeLearnMore() {
        modifyReservationWidget.viewModel.helpDialogRes = R.string.itin_flight_modify_widget_change_reservation_dialog_text
        modifyReservationWidget.changeLearnMoreText.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Change.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun testOminitureForCancelLearnMore() {
        modifyReservationWidget.viewModel.helpDialogRes = R.string.itin_flight_modify_widget_cancel_reservation_dialog_text
        modifyReservationWidget.cancelLearnMoreText.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Cancel.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun testChangeLearnMoreClick() {
        modifyReservationWidget.viewModel.helpDialogRes = R.string.itin_flight_modify_widget_change_reservation_dialog_text
        modifyReservationWidget.changeLearnMoreText.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(activity.getString(R.string.itin_flight_modify_widget_change_reservation_dialog_text), dialogContentView.text.toString())
    }

    @Test
    fun testCancelLearnMoreClick() {
        modifyReservationWidget.viewModel.helpDialogRes = R.string.itin_flight_modify_widget_cancel_reservation_dialog_text
        modifyReservationWidget.cancelLearnMoreText.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(activity.getString(R.string.itin_flight_modify_widget_cancel_reservation_dialog_text), dialogContentView.text.toString())
    }

    @Test
    fun testChangeAndCancelLearnMoreClick() {
        modifyReservationWidget.viewModel.helpDialogRes = R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text
        modifyReservationWidget.changeLearnMoreText.performClick()
        var alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        var dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        val customerSupportButton = alertDialog.findViewById<View>(R.id.dialog_customer_support)
        assertEquals(true, alertDialog.isShowing)
        assertEquals(activity.getString(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text), dialogContentView.text.toString())

        modifyReservationWidget.cancelLearnMoreText.performClick()
        alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        dialogContentView = alertDialog.findViewById<View>(R.id.dialog_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(activity.getString(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text), dialogContentView.text.toString())

        customerSupportButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Call.Expedia", mockAnalyticsProvider)
    }
}
