package com.expedia.bookings.widget.shared

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel
import com.mobiata.android.util.SettingUtils

open class BaseWebViewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.web_view_toolbar)

    var webViewPopUp: WebView? = null

    val container by bindView<FrameLayout>(R.id.web_container)
    var webView = WebView(context)
    open fun chromeClient() = WebChromeClient()

    private fun addNewWebViewToWidget(context: Context): WebView {
        webView.visibility = View.GONE
        container.removeView(webView)

        val webView = WebView(context)
        webView.webViewClient = webClient()
        webView.webChromeClient = chromeClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportMultipleWindows(true)

        webView.setDownloadListener { url, _, _, _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        container.addView(webView, 0)

        return webView
    }

    val progressView: ProgressBar by bindView(R.id.webview_progress_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }

    fun webClient() = object : WebViewClient() {

        //Using this deprecated API in order to support the shouldOverrideUrlLoading function for L and M devices
        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val endPointURL = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl
            if (url.contains("/user/signin")) {
                view.stopLoading()
                NavUtils.showAccountSignIn(context)
                webView.goBack()
                webView.clearHistory()
            } else if (!url.startsWith(endPointURL) && url.startsWith("http")) {
                webViewPopUp?.visibility = View.VISIBLE
                webViewPopUp?.loadUrl(url)
                webView.visibility = View.GONE
            } else {
                hideWebViewPopUp()
                webView.loadUrl(url)
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            onPageFinished(url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            if (url.startsWith("http") || url.startsWith(context.getString(R.string.clear_webview_url))) {
                onWebPageStarted(view, url, favicon)
            } else {
                view.stopLoading()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            val httpsSecurityDisabled = SettingUtils.get(context, context.getString(R.string.preference_disable_modern_https_security), false)
            if (BuildConfig.DEBUG && httpsSecurityDisabled) {
                handler?.proceed()
            } else {
                super.onReceivedSslError(view, handler, error)
            }
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            toggleLoading(false)
        }
    }

    protected fun hideWebViewPopUp() {
        webViewPopUp?.let {
            webViewPopUp!!.visibility = View.GONE
            webView.visibility = View.VISIBLE
            container.removeView(webViewPopUp)
            webViewPopUp = null
        }
    }

    open fun onPageFinished(url: String) {
        redirectSigninClick()
        toggleLoading(false)
    }

    private fun redirectSigninClick() {
        webView.evaluateJavascript("var elements = $('a').filter(function(){ " +
                "return(this.getAttribute('id') == 'login-module-title'" +
                ");});" +
                "$.each(elements, function(key, value) { " +
                "value.onclick = function(event) { " +
                "event.stopPropagation(); " +
                "document.location.href = '/user/signin';" +
                "}});", null)
    }

    open fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        toggleLoading(true)
    }

    init {
        View.inflate(getContext(), R.layout.widget_web_view, this)
        this.orientation = LinearLayout.VERTICAL
        toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
        setToolbarPadding()
    }

    open var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewURLObservable.subscribe { url ->
            webView = addNewWebViewToWidget(context)
            webView.loadUrl(AppAnalytics.getUrlWithVisitorData(url))
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

    override fun onFinishInflate() {
        super.onFinishInflate()
        webView = addNewWebViewToWidget(context)
    }
}
