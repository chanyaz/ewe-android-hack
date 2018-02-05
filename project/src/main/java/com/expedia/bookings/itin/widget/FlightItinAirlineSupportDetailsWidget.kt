package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.vm.FlightItinAirlineSupportDetailsViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class FlightItinAirlineSupportDetailsWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val title by bindView<TextView>(R.id.airline_support_help_title)
    val airlineSupport by bindView<TextView>(R.id.airline_support)
    val ticket by bindView<TextView>(R.id.airline_support_ticket)
    val confirmation by bindView<TextView>(R.id.airline_support_confirmation)
    val itinerary by bindView<TextView>(R.id.airline_support_itinerary)

    val customerSupportCallButton by bindView<TextView>(R.id.airline_call_support_action_button)
    val customerSupportSiteButton by bindView<TextView>(R.id.airline_customer_support_site_button)

    init {
        View.inflate(context, R.layout.widget_flight_itin_airline_support_detail, this)
    }

    var viewModel: FlightItinAirlineSupportDetailsViewModel by notNullAndObservable { vm ->
        vm.airlineSupportDetailsWidgetSubject.subscribe { params ->
            setUpWidget(params)
        }
    }

    private fun setUpWidget(param: FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams) {
        title.text = param.title
        airlineSupport.text = param.airlineSupport
        if (Strings.isNotEmpty(param.ticket)) {
            ticket.visibility = View.VISIBLE
            ticket.text = Phrase.from(context, R.string.itin_flight_airline_support_widget_ticket_TEMPLATE).put("ticket_number", param.ticket).format().toString()
            ticket.contentDescription = Phrase.from(context, R.string.itin_flight_airline_support_widget_ticket_content_description_TEMPLATE).put("ticket_number", getNumbersForContentDescription(param.ticket)).format().toString()
            onTicketClick(param.ticket)
        }
        if (Strings.isNotEmpty(param.confirmation)) {
            confirmation.visibility = View.VISIBLE
            confirmation.text = Phrase.from(context, R.string.itin_flight_airline_support_widget_confirmation_TEMPLATE).put("confirmation_number", param.confirmation).format().toString()
            confirmation.contentDescription = Phrase.from(context, R.string.itin_flight_airline_support_widget_confirmation_content_description_TEMPLATE).put("confirmation_number", getNumbersForContentDescription(param.confirmation)).format().toString()
            onConfirmationClick(param.confirmation)
        }
        if (Strings.isNotEmpty(param.itinerary)) {
            itinerary.visibility = View.VISIBLE
            itinerary.text = Phrase.from(context, R.string.itin_flight_airline_support_widget_itinerary_TEMPLATE).put("itinerary_number", param.itinerary).format().toString()
            itinerary.contentDescription = Phrase.from(context, R.string.itin_flight_airline_support_widget_itinerary_content_description_TEMPLATE).put("itinerary_number", getNumbersForContentDescription(param.itinerary)).format().toString()
            onItineraryClick(param.itinerary)
        }
        if (Strings.isNotEmpty(param.callSupport)) {
            customerSupportCallButton.visibility = View.VISIBLE
            customerSupportCallButton.text = param.callSupport
            onCustomerSupportCallButtonClick(param.callSupport)
        }
        if (Strings.isNotEmpty(param.siteSupportURL)) {
            customerSupportSiteButton.visibility = View.VISIBLE
            customerSupportSiteButton.text = param.siteSupportText
            onCustomerSupportWebButtonClick(param.siteSupportURL)
            AccessibilityUtil.appendRoleContDesc(customerSupportSiteButton, customerSupportSiteButton.text.toString(), R.string.accessibility_cont_desc_role_button)
        }
    }

    private fun onCustomerSupportCallButtonClick(supportNumber: String) = customerSupportCallButton.setOnClickListener {
        OmnitureTracking.trackFlightItinAirlineSupportCallClick()
        if (Strings.isNotEmpty(supportNumber)) {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SocialUtils.call(context, supportNumber)
            } else {
                copyToClipBoard(supportNumber)
            }
        }
    }

    private fun onCustomerSupportWebButtonClick(url: String) = customerSupportSiteButton.setOnClickListener {
        OmnitureTracking.trackFlightItinAirlineSupportWebsiteClick()
        context.startActivity(buildWebViewIntent(R.string.itin_flight_airline_support_widget_support_webview_title, url).intent)
    }

    private fun onTicketClick(text: String) = ticket.setOnClickListener {
        copyToClipBoard(text)
    }

    private fun onItineraryClick(text: String) = itinerary.setOnClickListener {
        copyToClipBoard(text)
    }

    private fun onConfirmationClick(text: String) = confirmation.setOnClickListener {
        copyToClipBoard(text)
    }

    private fun copyToClipBoard(text: String) {
        ClipboardUtils.setText(context, text)
        Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun buildWebViewIntent(title: Int, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        return builder
    }

    private fun getNumbersForContentDescription(numberSeparatedWithComma: String): String {
        val stringBuffer = StringBuffer()
        val numberList = numberSeparatedWithComma.split(",")
        for (number in numberList) {
            val contentDescriptionNumber = number.replace("", " ").trim()
            if (Strings.isNotEmpty(stringBuffer)) {
                stringBuffer.append(" , ")
            }
            stringBuffer.append(contentDescriptionNumber)
        }
        return stringBuffer.toString()
    }
}
