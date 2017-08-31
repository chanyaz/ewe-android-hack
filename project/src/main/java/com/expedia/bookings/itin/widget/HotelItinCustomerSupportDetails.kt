package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinCustomerSupportDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val customerSupportTextView: TextView by bindView(R.id.customer_support_text)
    val itineraryNumberTextView: TextView by bindView(R.id.itinerary_number)
    val callSupportActionButton: TextView by bindView(R.id.call_support_action_button)
    val customerSupportSiteButton: TextView by bindView(R.id.expedia_customer_support_site_button)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_customer_support, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        customerSupportTextView.text = Phrase.from(context, R.string.itin_hotel_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        itineraryNumberTextView.text = Phrase.from(context, R.string.itin_hotel_itinerary_number_TEMPLATE).put("itinnumber", itinCardDataHotel.tripNumber).format().toString()
        itineraryNumberTextView.contentDescription = Phrase.from(this, R.string.itin_hotel_manage_booking_itinerary_number_content_description_TEMPLATE)
                .put("number", itinCardDataHotel.tripNumber.replace(".".toRegex(), "$0 ")).format().toString()
        itineraryNumberTextView.setOnClickListener {
            ClipboardUtils.setText(context, itinCardDataHotel.tripNumber)
            Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }

        val supportNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser())
        callSupportActionButton.text = supportNumber
        callSupportActionButton.contentDescription = Phrase.from(context, R.string.itin_hotel_manage_booking_call_support_button_content_description_TEMPLATE).put("phonenumber", supportNumber).put("brand", BuildConfig.brand).format().toString()
        callSupportActionButton.setOnClickListener {
            if (supportNumber.isNotEmpty()) {
                val pm = context.packageManager
                if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                    SocialUtils.call(context, supportNumber)
                } else {
                    ClipboardUtils.setText(context, supportNumber)
                    Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                }
                OmnitureTracking.trackItinHotelCallSupport()
            }
        }

        customerSupportSiteButton.text = Phrase.from(context, R.string.itin_hotel_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        customerSupportSiteButton.contentDescription = Phrase.from(context, R.string.itin_hotel_customer_support_site_button_content_description_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        customerSupportSiteButton.setOnClickListener {
            context.startActivity(buildWebViewIntent(R.string.itin_hotel_customer_support_site_toolbar_header, PointOfSale.getPointOfSale().bookingSupportUrl).intent)
            OmnitureTracking.trackItinHotelOpenSupportWebsite()
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