package com.expedia.bookings.itin.utils

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.activity.WebViewActivityWithToolbar
import com.expedia.bookings.utils.Constants

class WebViewLauncher(val context: Context) : IWebViewLauncher {

    override fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?) {
        val animation: Bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, R.anim.slide_down_partially).toBundle()
        val webViewIntent = buildSharableWebViewIntent(title, url, anchor, tripId).intent
        (context as AppCompatActivity).startActivityForResult(webViewIntent, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE, animation)
    }

    private fun buildSharableWebViewIntent(title: String, url: String?, anchor: String?, tripId: String?): WebViewActivity.IntentBuilder {
        val builder: WebViewActivityWithToolbar.IntentBuilder = WebViewActivityWithToolbar.IntentBuilder(context)
        with(builder) {
            if (anchor != null) setUrlWithAnchor(url, anchor) else setUrl(url)
            setTitle(title)
            setInjectExpediaCookies(true)
            setAllowMobileRedirects(false)
            if (anchor != null) setItinTripIdForRefresh(tripId)
        }
        return builder
    }

    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String, scrapTitle: Boolean) {
        val animation: Bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, R.anim.slide_down_partially).toBundle()
        val webViewIntent = buildWebViewIntent(title, url, anchor, tripId, scrapTitle).intent
        (context as AppCompatActivity).startActivityForResult(webViewIntent, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE, animation)
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?, tripId: String, scrapTitle: Boolean): WebViewActivity.IntentBuilder {
        val builder = WebViewActivity.IntentBuilder(context)
        with(builder) {
            if (anchor != null) setUrlWithAnchor(url, anchor) else setUrl(url)
            setTitle(title)
            setInjectExpediaCookies(true)
            setAllowMobileRedirects(false)
            setOriginalWebViewTitle(scrapTitle)
            setItinTripIdForRefresh(tripId)
        }
        return builder
    }
}
