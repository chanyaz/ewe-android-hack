package com.expedia.bookings.itin.utils

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.utils.Constants

class WebViewLauncher(val context: Context) : IWebViewLauncher {

    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String) {
        val webViewIntent = buildWebViewIntent(title, url, anchor, tripId).intent
        (context as AppCompatActivity).startActivityForResult(webViewIntent, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE)
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?, tripId: String): WebViewActivity.IntentBuilder {
        val builder = WebViewActivity.IntentBuilder(context)
        with(builder) {
            if (anchor != null) setUrlWithAnchor(url, anchor) else setUrl(url)
            setTitle(title)
            setInjectExpediaCookies(true)
            setAllowMobileRedirects(false)
            setItinTripIdForRefresh(tripId)
        }
        return builder
    }
}
