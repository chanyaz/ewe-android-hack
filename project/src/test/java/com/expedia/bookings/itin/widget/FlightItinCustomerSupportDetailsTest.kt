package com.expedia.bookings.itin.widget

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinCustomerSupportDetailsViewModel
import com.expedia.bookings.itin.vm.ItinCustomerSupportDetailsViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinCustomerSupportDetailsTest {
    lateinit var customerSupportWidget: FlightItinCustomerSupportDetails
    lateinit var context: Context

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        customerSupportWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_customer_support_details, null) as FlightItinCustomerSupportDetails
    }

    @Test
    fun testWidgetText() {
        customerSupportWidget.viewModel = FlightItinCustomerSupportDetailsViewModel()
        val header = Phrase.from(context, R.string.itin_flight_customer_support_header_text_TEMPLATE).put("brand", "Expedia").format().toString()
        val itineraryNumb = Phrase.from(context, R.string.itin_flight_itinerary_number_TEMPLATE).put("itin_number", "12345678").format().toString()
        val customerSupportNumber = "+1-866-230-3837"
        val customerSupportButton = Phrase.from(context, R.string.itin_flight_customer_support_site_header_TEMPLATE).put("brand", "Expedia").format().toString()
        val customerSupportURL = "http://www.expedia.com/service/"
        customerSupportWidget.viewModel.updateItinCustomerSupportDetailsWidgetSubject.onNext(ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams(header, itineraryNumb, customerSupportNumber, customerSupportButton, customerSupportURL))
        assertEquals(customerSupportWidget.customerSupportTextView.text, header)
        assertEquals(customerSupportWidget.itineraryNumberTextView.text, itineraryNumb)
        assertEquals(customerSupportWidget.callSupportActionButton.text, customerSupportNumber)
        assertEquals(customerSupportWidget.customerSupportSiteButton.text, customerSupportButton)
    }
}

