package com.expedia.bookings.webview

import android.app.Activity
import android.net.MailTo
import android.webkit.WebView
import android.webkit.WebViewClient
import com.expedia.bookings.utils.DebugInfoUtils
import com.mobiata.android.SocialUtils

open class BaseWebViewClient(val activity: Activity, val loadCookies: Boolean): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
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

    // open for unit testing
    open fun doSupportEmail(url: String) {
        val mt = MailTo.parse(url)
        SocialUtils.email(activity, mt.to, "", DebugInfoUtils.generateEmailBody(activity))
    }
}
