package com.expedia.bookings.itin.utils

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.OmnitureTracking

object MessageHotelUtil {

    fun getClickListener(url: String, context: Context, fromManageBooking: Boolean = false): View.OnClickListener =
            View.OnClickListener {
                OmnitureTracking.trackItinHotelMessage(fromManageBooking)
                context.startActivity(buildWebViewIntentBuilder(url = url, context = context).intent,
                        ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0)
                                .toBundle())
            }

    private fun buildWebViewIntentBuilder(url: String, context: Context): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setUrl(url)
        builder.setTitle(context.getString(R.string.itin_hotel_details_message_hotel_button))
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setDomStorage(true)
        return builder
    }
}
