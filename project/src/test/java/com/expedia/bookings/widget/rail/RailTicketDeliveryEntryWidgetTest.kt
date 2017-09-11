package com.expedia.bookings.widget.rail

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailTicketDeliveryOption
import com.expedia.bookings.rail.widget.RailTicketDeliveryEntryWidget
import com.expedia.bookings.rail.widget.TicketDeliveryMethod
import com.expedia.bookings.section.RailDeliverySpinnerWithValidationIndicator
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TicketDeliverySelectionStatus
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTicketDeliveryEntryWidgetTest {

    var widget by Delegates.notNull<RailTicketDeliveryEntryWidget>()
    val context = RuntimeEnvironment.application

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Rail)
        widget = LayoutInflater.from(activity).inflate(R.layout.test_rail_ticket_delivery_entry_widget, null) as RailTicketDeliveryEntryWidget
    }

    @Test
    fun testInitialization() {
        val testStationContainerSelectionSubscriber = TestSubscriber.create<TicketDeliverySelectionStatus>()
        widget.stationContainer.viewModel.statusChanged.subscribe(testStationContainerSelectionSubscriber)
        val testMailDeliveryContainerSelectionSubscriber = TestSubscriber.create<TicketDeliverySelectionStatus>()
        widget.mailDeliveryContainer.viewModel.statusChanged.subscribe(testMailDeliveryContainerSelectionSubscriber)

        val viewModel = RailTicketDeliveryEntryViewModel(context)
        widget.viewModel = viewModel

        val testCloseSubscriber = TestSubscriber.create<Unit>()
        widget.closeSubject.subscribe(testCloseSubscriber)

        assertEquals(TicketDeliveryMethod.PICKUP_AT_STATION, viewModel.ticketDeliveryObservable.value)
        testStationContainerSelectionSubscriber.assertValueCount(1)
        assertEquals(TicketDeliverySelectionStatus.SELECTED, testStationContainerSelectionSubscriber.onNextEvents[0])
        testMailDeliveryContainerSelectionSubscriber.assertValueCount(1)
        assertEquals(TicketDeliverySelectionStatus.UNSELECTED, testMailDeliveryContainerSelectionSubscriber.onNextEvents[0])
        assertEquals(View.GONE, widget.mailShippingAddressContainer.visibility)

        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(1)
    }

    @Test
    fun testDeliveryByMail() {
        val testStationContainerSelectionSubscriber = TestSubscriber.create<TicketDeliverySelectionStatus>()
        widget.stationContainer.viewModel.statusChanged.subscribe(testStationContainerSelectionSubscriber)
        val testMailDeliveryContainerSelectionSubscriber = TestSubscriber.create<TicketDeliverySelectionStatus>()
        widget.mailDeliveryContainer.viewModel.statusChanged.subscribe(testMailDeliveryContainerSelectionSubscriber)

        val viewModel = RailTicketDeliveryEntryViewModel(context)
        widget.viewModel = viewModel

        val testCloseSubscriber = TestSubscriber.create<Unit>()
        widget.closeSubject.subscribe(testCloseSubscriber)

        widget.mailDeliveryContainer.performClick()
        assertEquals(TicketDeliveryMethod.DELIVER_BY_MAIL, viewModel.ticketDeliveryObservable.value)
        testStationContainerSelectionSubscriber.assertValueCount(2)
        assertEquals(TicketDeliverySelectionStatus.UNSELECTED, testStationContainerSelectionSubscriber.onNextEvents[1])
        testMailDeliveryContainerSelectionSubscriber.assertValueCount(2)
        assertEquals(TicketDeliverySelectionStatus.SELECTED, testMailDeliveryContainerSelectionSubscriber.onNextEvents[1])

        val ticketDeliveryOptionsAll = ArrayList<RailTicketDeliveryOption>()
        val option1 = RailTicketDeliveryOption()
        option1.ticketDeliveryCountryCodeList = listOf("GB")
        option1.ticketDeliveryDescription = "Delivery by post"
        option1.ticketDeliveryOptionToken = RailCreateTripResponse.RailTicketDeliveryOptionToken.SEND_BY_EXPRESS_POST_UK
        ticketDeliveryOptionsAll.add(option1)

        viewModel.ticketDeliveryOptions.onNext(ticketDeliveryOptionsAll)

        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(0)

        val deliveryOptionView = widget.deliveryAddressEntry.findViewById<View>(R.id.edit_delivery_option_spinner) as RailDeliverySpinnerWithValidationIndicator
        val addressLineOne = widget.deliveryAddressEntry.findViewById<View>(R.id.edit_address_line_one) as TextView
        val city = widget.deliveryAddressEntry.findViewById<View>(R.id.edit_address_city) as TextView
        val postalCode = widget.deliveryAddressEntry.findViewById<View>(R.id.edit_address_postal_code) as TextView

        // Required field in mailing address not filled
        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(0)

        // Only delivery option selected
        deliveryOptionView.spinner.setSelection(0)
        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(0)

        // Address line one filled.
        addressLineOne.text = "Address One"
        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(0)

        // City filled
        city.text = "City"
        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(0)

        // All required fields are filled
        postalCode.text = "Postal Code"
        widget.toolbarViewModel.doneClicked.onNext(Unit)
        testCloseSubscriber.assertValueCount(1)
    }
}