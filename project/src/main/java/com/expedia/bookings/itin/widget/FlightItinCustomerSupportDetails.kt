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
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.mobiata.android.SocialUtils

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
            setUpListeners(param)
        }
    }

    private fun setUpWidgets(param: ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams) {
        customerSupportTextView.text = param.header
        itineraryNumberTextView.text = param.itineraryNumber
        callSupportActionButton.text = param.callSupportNumber
        customerSupportSiteButton.text = param.customerSupport
    }

    private fun setUpListeners(param: ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams) {
        onCustomerSupportSiteButtonClick(param)
        onCallSupportActionButtonClick(param)
    }

    private fun onCustomerSupportSiteButtonClick(param: ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams) = customerSupportSiteButton.setOnClickListener {
        context.startActivity(buildWebViewIntent(R.string.itin_flight_customer_support_site_toolbar_header, param.customerSupportURL).intent)
        OmnitureTracking.trackItinFlightOpenSupportWebsite()
    }

    private fun onCallSupportActionButtonClick(param: ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams) = callSupportActionButton.setOnClickListener {
        val supportNumber = param.callSupportNumber
        if (supportNumber.isNotEmpty()) {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SocialUtils.call(context, supportNumber)
            } else {
                ClipboardUtils.setText(context, supportNumber)
                Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
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
}
