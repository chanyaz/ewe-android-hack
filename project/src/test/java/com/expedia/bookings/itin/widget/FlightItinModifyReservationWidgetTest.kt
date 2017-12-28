package com.expedia.bookings.itin.widget

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinModifyReservationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinModifyReservationWidgetTest {
    lateinit var activity: Activity
    lateinit var modifyReservationWidget: FlightItinModifyReservationWidget

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        modifyReservationWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_modify_reservation_widget, null) as FlightItinModifyReservationWidget
        modifyReservationWidget.viewModel = FlightItinModifyReservationViewModel()
    }

    @Test
    fun testViewVisibility() {
        val webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"
        val webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        val params = FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(webChangePathURL, true, webCancelPathURL, true)
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, true)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, true)

        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, "", false))
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, false)

        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", true, "", true))
        modifyReservationWidget.viewModel.modifyReservationSubject.onNext(params)
        assertEquals(modifyReservationWidget.cancelReservationButton.isEnabled, false)
        assertEquals(modifyReservationWidget.changeReservationButton.isEnabled, false)
    }
}