package com.expedia.bookings.test

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.presenter.flight.FlightSearchPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightCabinClassPickerView
import com.expedia.vm.flights.FlightCabinClassViewModel
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCabinClassViewModelTest {

    private var widget: FlightSearchPresenter by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    var server: MockWebServer = MockWebServer()
        @Rule get

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
                null) as FlightSearchPresenter
    }

    @Test
    fun testFlightCabinClass() {
        val flightCabinClassWidget = widget.flightCabinClassWidget
        flightCabinClassWidget.performClick()
        val view = flightCabinClassWidget.flightCabinClassDialogView

        val flightCabinClassPickerView = view.findViewById<FlightCabinClassPickerView>(R.id.flight_class_view)
        flightCabinClassPickerView.businessClassRadioButton.performClick()
        flightCabinClassWidget.dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        val flightCabinClassViewModel: FlightCabinClassViewModel = flightCabinClassWidget.flightCabinClassView.viewmodel
        val flightSelectedCabinClassIdTestSubscriber = TestObserver<Int>()
        flightCabinClassViewModel.flightSelectedCabinClassIdObservable.subscribe(flightSelectedCabinClassIdTestSubscriber)

        assertEquals(FlightServiceClassType.CabinCode.BUSINESS, flightCabinClassViewModel.flightCabinClassObservable.value)

        flightCabinClassWidget.performClick()
        flightCabinClassPickerView.firstClassRadioButton.performClick()
        flightCabinClassWidget.dialog.dismiss()
        flightSelectedCabinClassIdTestSubscriber.assertValueCount(1)
        flightSelectedCabinClassIdTestSubscriber.assertValue(flightCabinClassPickerView.getIdByClass(FlightServiceClassType.CabinCode.BUSINESS))
    }
}
