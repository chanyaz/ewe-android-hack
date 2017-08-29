package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel

open class BaseWebViewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val HEADER_CLASS = "site-header-primary"
    val FACEBOOK_LOGIN_CLASS = "facebook-login-pane"

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val webView: WebView by bindView(R.id.web_view)
    val progressView: ProgressBar by bindView(R.id.webview_progress_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }

    var webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            onPageFinished(url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            onWebPageStarted(view, url, favicon)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            toggleLoading(false)
        }
    }

    open fun onPageFinished(url: String) {
        preventLoadingOfDivClass(HEADER_CLASS)
        preventLoadingOfDivClass(FACEBOOK_LOGIN_CLASS)
        toggleLoading(false)
    }

    private fun preventLoadingOfDivClass(className: String) {
        webView.loadUrl("javascript:(function() { document.getElementsByClassName('$className')[0].style.display=\"none\"; })()")
    }

    open fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        toggleLoading(true)
    }

    init {
        View.inflate(getContext(), R.layout.widget_web_view, this)
        this.orientation = LinearLayout.VERTICAL
        toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
        setToolbarPadding()

        webView.setWebViewClient(webClient)
        webView.settings.javaScriptEnabled = true
    }

    open var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewURLObservable.subscribe { url ->
            webView.loadUrl(ADMS_Measurement.getUrlWithVisitorData(url))
        }
    }

    open fun setExitButtonOnClickListener(listener: OnClickListener) {
        toolbar.setNavigationOnClickListener(listener)
    }

    open fun setToolbarPadding() {
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    open fun toggleLoading(loading: Boolean) {
        if (ExpediaBookingApp.isAutomation()) {
            return
        }
        if (loading) {
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
        }
    }
}
