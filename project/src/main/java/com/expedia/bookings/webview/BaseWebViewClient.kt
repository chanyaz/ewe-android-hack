package com.expedia.bookings.webview

import android.app.Activity
import android.graphics.Bitmap
import android.net.MailTo
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.expedia.bookings.features.Features
import com.expedia.bookings.fragment.WebViewFragment
import com.expedia.bookings.tracking.CarWebViewTracking
import com.expedia.bookings.tracking.PackageWebViewTracking
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
        } else if (url.contains("ubr.to/2noQerV")) {
            // This is required to prevent the app WebView page from showing URL Exception
            view.stopLoading()
            SocialUtils.openApp(activity, url)
            return false
        } else if (loadCookies) {
            if (mTrackingName != WebViewFragment.TrackingName.PackageWebView) {
                view.loadUrl(url)
            }
            return false
        } else {
            @Suppress("DEPRECATION")
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (isUserAccountUrl(url)) {
            view?.visibility = View.INVISIBLE
        }
    }

    override fun onPageFinished(webView: WebView, url: String) {

        if (isRulesAndRestrictionUrl(url)) {
            injectRulesAndRestrictionsJavascript(webView)
        }

        if (isUserAccountUrl(url)) {
            webView.visibility = View.VISIBLE
        }
    }

    override fun onLoadResource(webView: WebView, url: String) {
        super.onLoadResource(webView, url)

        if (isUserAccountUrl(url) && Features.all.accountWebViewInjections.enabled()) {
            injectAccountPageJavascript(webView)
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
        if (mTrackingName == WebViewFragment.TrackingName.PackageWebView) {
            PackageWebViewTracking.trackAppPackageWebViewLogOut()
        }
    }

    private fun injectAccountPageJavascript(webView: WebView) {
        webView.loadUrl("javascript:(function() { " +
                "document.getElementById('div_rewardsHeader').style.display='none'; " +
                "})()")
        webView.loadUrl("javascript:(function() { " +
                "document.getElementById('acc_div').style.display='none'; " +
                "})()")
        webView.loadUrl("javascript:(function() { " +
                "document.querySelector('.tabs.cf').style.display='none'; " +
                "})()")

        webView.loadUrl("javascript:(function() { " +
                "document.querySelector('.site-header').style.display='none'; " +
                "})()")
    }

    private fun injectRulesAndRestrictionsJavascript(webView: WebView) {
        // Hide Print button in Rules And Restriction Page
        webView.loadUrl("javascript:(function() { " +
                "document.querySelector('button.print-link').style.display='none'; " +
                "})()")

        // Hide close button in Rules and Restriction
        webView.loadUrl("javascript:(function() { " +
                "document.querySelector('button.close-button').style.display='none'; " +
                "})()")
    }

    private fun isRulesAndRestrictionUrl(url: String): Boolean = (url.contains("RulesAndRestrictions"))
    private fun isLogOutUrl(url: String): Boolean = url.contains("/user/logout")
    private fun isUserAccountUrl(url: String): Boolean = url.contains("/user/account")
}
