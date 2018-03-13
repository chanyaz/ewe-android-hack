package com.expedia.bookings.itin.widget

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.flight.manageBooking.FlightItinLegsDetailWidgetViewModel
import com.expedia.bookings.itin.flight.manageBooking.FlightItinLegsDetailWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinLegsDetailWidgetTest {

    lateinit var legsDetailsWidget: FlightItinLegsDetailWidget

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        legsDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_legs_detail_widget, null) as FlightItinLegsDetailWidget
    }

    @Test
    fun testRulesAndRestrictionDialogWithdata() {
        val cancelChange = "We understand that sometimes plans change. We do not charge a cancel or change fee. When the airline charges such fees in accordance with its own policies, the cost will be passed on to you."
        legsDetailsWidget.viewModel = FlightItinLegsDetailWidgetViewModel()
        legsDetailsWidget.viewModel.rulesAndRestrictionDialogTextSubject.onNext(cancelChange)
        assertEquals(View.VISIBLE, legsDetailsWidget.rulesAndRegulation.visibility)
        assertEquals(View.VISIBLE, legsDetailsWidget.rulesAndRegulationDivider.visibility)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        legsDetailsWidget.rulesAndRegulation.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.AirlineRules", mockAnalyticsProvider)
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val rulesText = alertDialog.findViewById<View>(R.id.fragment_dialog_scrollable_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals(cancelChange, rulesText.text.toString())
    }

    @Test
    fun testRulesAndRestrictionDialogWithoutdata() {
        legsDetailsWidget.viewModel = FlightItinLegsDetailWidgetViewModel()
        legsDetailsWidget.viewModel.rulesAndRestrictionDialogTextSubject.onNext("")
        assertEquals(View.GONE, legsDetailsWidget.rulesAndRegulation.visibility)
        assertEquals(View.GONE, legsDetailsWidget.rulesAndRegulationDivider.visibility)
    }

    @Test
    fun testSplitTicketTextVisibility() {
        legsDetailsWidget.viewModel = FlightItinLegsDetailWidgetViewModel()
        legsDetailsWidget.viewModel.shouldShowSplitTicketTextSubject.onNext(false)
        assertEquals(View.GONE, legsDetailsWidget.splitTicketText.visibility)
        assertEquals(View.GONE, legsDetailsWidget.splitTicketDividerView.visibility)

        legsDetailsWidget.viewModel.shouldShowSplitTicketTextSubject.onNext(true)
        assertEquals(View.VISIBLE, legsDetailsWidget.splitTicketText.visibility)
        assertEquals(View.VISIBLE, legsDetailsWidget.splitTicketDividerView.visibility)
    }
}
