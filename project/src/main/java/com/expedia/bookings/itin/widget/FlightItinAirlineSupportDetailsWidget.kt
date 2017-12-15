package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
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
            setUpListeners(params)
        }
    }

    private fun setUpWidget(param: FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams) {
        title.text = param.title
        airlineSupport.text = param.airlineSupport
        ticket.text = param.ticket
        ticket.contentDescription = getContentDescriptionForView(ticket)
        if (Strings.isEmpty(param.ticket)) {
            ticket.visibility = View.GONE
        }
        confirmation.text = param.confirmation
        confirmation.contentDescription = getContentDescriptionForView(confirmation)
        if (Strings.isEmpty(param.confirmation)) {
            confirmation.visibility = View.GONE
        }
        itinerary.text = param.itinerary
        itinerary.contentDescription = getContentDescriptionForView(itinerary)
        if (Strings.isEmpty(param.itinerary)) {
            itinerary.visibility = View.GONE
        }
        customerSupportCallButton.text = param.callSupport
        if (Strings.isEmpty(param.callSupport)) {
            customerSupportCallButton.visibility = View.GONE
        }
        customerSupportSiteButton.text = param.siteSupportText
        AccessibilityUtil.appendRoleContDesc(customerSupportSiteButton, customerSupportSiteButton.text.toString(), R.string.accessibility_cont_desc_role_button)
        if (Strings.isEmpty(param.siteSupportURL)) {
            customerSupportSiteButton.visibility = View.GONE
        }
    }

    private fun onCustomerSupportCallButtonClick(param: FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams) = customerSupportCallButton.setOnClickListener {
        OmnitureTracking.trackFlightItinAirlineSupportCallClick()
        val supportNumber = param.callSupport
        if (Strings.isNotEmpty(supportNumber)) {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SocialUtils.call(context, supportNumber)
            } else {
                ClipboardUtils.setText(context, supportNumber)
                Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpListeners(param: FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams) {
        onCustomerSupportWebButtonClick(param)
        onCustomerSupportCallButtonClick(param)
    }


    private fun onCustomerSupportWebButtonClick(param: FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams) = customerSupportSiteButton.setOnClickListener {
        OmnitureTracking.trackFlightItinAirlineSupportWebsiteClick()
        context.startActivity(buildWebViewIntent(R.string.itin_flight_airline_support_widget_support_webview_title, param.siteSupportURL).intent)
    }

    private fun buildWebViewIntent(title: Int, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)

        return builder
    }

    private fun getContentDescriptionForView(view: TextView): String {
        val viewText = view.text.toString()
        if (viewText.contains("#")) {
            val numberText = context.getString(R.string.itin_flight_airline_support_widget_number_text)
            val numberSeparatedWithComma = viewText.substring(viewText.lastIndexOf("#") + 1).trim()
            val contentDescriptionText = viewText.replace("#", numberText)
            return contentDescriptionText.replace(numberSeparatedWithComma, getNumbersForContentDescription(numberSeparatedWithComma))
        }
        return viewText
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