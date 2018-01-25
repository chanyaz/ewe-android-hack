package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.vm.ItinCustomerSupportDetailsViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class FlightItinCustomerSupportDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val customerSupportTextView: TextView by bindView(R.id.customer_support_text)
    val itineraryNumberTextView: TextView by bindView(R.id.itinerary_number)
    val callSupportActionButton: TextView by bindView(R.id.call_support_action_button)
    val customerSupportSiteButton: TextView by bindView(R.id.expedia_customer_support_site_button)

    init {
        View.inflate(context, R.layout.widget_flight_itin_customer_support, this)
    }

    var viewModel: ItinCustomerSupportDetailsViewModel by notNullAndObservable { vm ->
        vm.updateItinCustomerSupportDetailsWidgetSubject.subscribe { param ->
            setUpWidgets(param)
        }
    }

    private fun setUpWidgets(param: ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams) {
        customerSupportTextView.text = param.header
        if (Strings.isNotEmpty(param.itineraryNumber)) {
            itineraryNumberTextView.visibility = View.VISIBLE
            itineraryNumberTextView.text = Phrase.from(context, R.string.itin_flight_itinerary_number_TEMPLATE).put("itin_number", param.itineraryNumber).format().toString()
            itineraryNumberTextView.contentDescription = Phrase.from(context, R.string.itin_flight_itinerary_number_content_description_TEMPLATE).put("itin_number", getNumbersForContentDescription(param.itineraryNumber)).format().toString()
            onItineraryClick(param.itineraryNumber)
        }
        if (Strings.isNotEmpty(param.callSupportNumber)) {
            callSupportActionButton.visibility = View.VISIBLE
            callSupportActionButton.text = param.callSupportNumber
            onCallSupportActionButtonClick(param.callSupportNumber)
        }
        if (Strings.isNotEmpty(param.customerSupportURL)) {
            customerSupportSiteButton.visibility = View.VISIBLE
            customerSupportSiteButton.text = param.customerSupport
            onCustomerSupportSiteButtonClick(param.customerSupportURL)
        }
    }

    private fun onCustomerSupportSiteButtonClick(url: String) = customerSupportSiteButton.setOnClickListener {
        context.startActivity(buildWebViewIntent(R.string.itin_flight_customer_support_site_toolbar_header, url).intent)
        OmnitureTracking.trackItinFlightOpenSupportWebsite()
    }

    private fun onCallSupportActionButtonClick(supportNumber: String) = callSupportActionButton.setOnClickListener {
        if (supportNumber.isNotEmpty()) {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SocialUtils.call(context, supportNumber)
            } else {
                copyToClipBoard(supportNumber)
            }
            OmnitureTracking.trackItinFlightCallSupport()
        }
    }

    private fun buildWebViewIntent(title: Int, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        return builder
    }

    private fun copyToClipBoard(text: String) {
        ClipboardUtils.setText(context, text)
        Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun onItineraryClick(text: String) = itineraryNumberTextView.setOnClickListener {
        copyToClipBoard(text)
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
