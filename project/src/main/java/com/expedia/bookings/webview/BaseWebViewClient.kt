package com.expedia.bookings.webview

import android.app.Activity
import android.graphics.Bitmap
import android.net.MailTo
import android.webkit.WebView
import android.webkit.WebViewClient
import com.expedia.bookings.fragment.WebViewFragment
import com.expedia.bookings.tracking.CarWebViewTracking
import com.expedia.bookings.tracking.RailWebViewTracking
import com.expedia.bookings.utils.DebugInfoUtils
import com.mobiata.android.SocialUtils

open class BaseWebViewClient(val activity: Activity, val loadCookies: Boolean,
                             val mTrackingName: WebViewFragment.TrackingName?) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (isLogOutUrl(url)) {
            trackLogOutClick()
        }
        if (url.startsWith("mailto:")) {
            doSupportEmail(url)
            return true
        } else if ("expda://success" == url || "expda://dismiss" == url) {
            // ignore success/dismiss links for now
            // more detail: https://github.com/ExpediaInc/ewe-ios-eb/wiki/WebView-hosted-ExpWeb-communication-to-Native-app
            activity.finish()
            return true
        } else if (loadCookies) {
            view.loadUrl(url)
            return false
        } else {
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(webView: WebView, url: String) {

        if (isRulesAndRestrictionUrl(url)) {

            // Hide Print button in Rules And Restriction Page
            webView.loadUrl("javascript:(function() { " +
                    "document.querySelector('button.print-link').style.display='none'; " +
                    "})()")

            // Hide close button in Rules and Restriction
            webView.loadUrl("javascript:(function() { " +
                    "document.querySelector('button.close-button').style.display='none'; " +
                    "})()")
        }
    }

    // open for unit testing
    open fun doSupportEmail(url: String) {
        val mt = MailTo.parse(url)
        SocialUtils.email(activity, mt.to, "", DebugInfoUtils.generateEmailBody(activity))
    }

    private fun trackLogOutClick() {
        if (mTrackingName == WebViewFragment.TrackingName.CarWebView) {
            CarWebViewTracking().trackAppCarWebViewLogOut()
        }
        if (mTrackingName == WebViewFragment.TrackingName.RailWebView) {
            RailWebViewTracking.trackAppRailWebViewLogOut()
        }
    }

    private fun isRulesAndRestrictionUrl(url: String): Boolean = (url.contains("RulesAndRestrictions"))
    private fun isLogOutUrl(url: String): Boolean = url.contains("/user/logout")
}

