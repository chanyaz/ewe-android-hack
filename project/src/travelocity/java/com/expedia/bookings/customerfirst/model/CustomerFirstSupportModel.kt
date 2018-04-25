package com.expedia.bookings.customerfirst.model

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.squareup.phrase.Phrase

enum class CustomerFirstSupportModel(val nameResId: Int, val titleResId: Int, val uriResId: Int, val packageNameResId: Int, val iconResId: Int) {
    TWITTER(R.string.twitter, R.string.customer_first_connect_with_twitter, R.string.customer_first_twitter_uri, R.string.twitter_package_name, R.drawable.ic_twitter),
    FACEBOOK(R.string.facebook_messenger, R.string.customer_first_connect_with_fb, R.string.customer_first_facebook_messenger_uri, R.string.facebook_messenger_package_name, R.drawable.ic_facebook),
    PHONE_CALL(0, R.string.customer_first_phone_number, R.string.customer_first_phone_number, 0, R.drawable.ic_customer_first_phone),
    HELP_TOPICS(0, R.string.customer_first_help_topics, R.string.customer_first_help_topics_url, 0, R.drawable.ic_help);

    fun trackCustomerSupportDownloadClick() {
        if (this == TWITTER) {
            OmnitureTracking.trackCustomerFirstTwitterDownloadClick()
        } else {
            OmnitureTracking.trackCustomerFirstMessengerDownloadClick()
        }
    }

    fun trackCustomerSupportDownloadCancelClick() {
        if (this == TWITTER) {
            OmnitureTracking.trackCustomerFirstTwitterDownloadCancelClick()
        } else {
            OmnitureTracking.trackCustomerFirstMessengerDownloadCancelClick()
        }
    }

    fun trackCustomerSupportOpenAppClick() {
        if (this == TWITTER) {
            OmnitureTracking.trackCustomerFirstTwitterOpenClick()
        } else {
            OmnitureTracking.trackCustomerFirstMessengerOpenClick()
        }
    }

    fun trackCustomerSupportOpenCancelClick() {
        if (this == TWITTER) {
            OmnitureTracking.trackCustomerFirstTwitterOpenCancelClick()
        } else {
            OmnitureTracking.trackCustomerFirstMessengerOpenCancelClick()
        }
    }

    fun getCustomerSupportContDescString(context: Context): String {
        return if (this == PHONE_CALL) {
            Phrase.from(context, R.string.customer_first_phone_number_cont_desc_TEMPLATE)
                    .put("phone_number", context.getString(titleResId))
                    .format().toString()
        } else {
            context.getString(titleResId)
        }
    }
}
