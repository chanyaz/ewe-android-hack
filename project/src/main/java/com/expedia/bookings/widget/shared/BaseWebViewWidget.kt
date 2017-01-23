package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel
import rx.subjects.PublishSubject

open class BaseWebViewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val webView: WebView by bindView(R.id.web_view)
    val progressView: ProgressBar by bindView(R.id.webview_progress_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val closeWebView = PublishSubject.create<Unit>()

    var webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            toggleLoading(false)
       //     CookieManager.getInstance().flush()
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            toggleLoading(true)
            if (url?.contains("onfirmation") ?: false) {
                closeWebView.onNext(Unit)
            }
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            toggleLoading(false)
        }
    }

    private fun loadCookies() {

        val cookieSyncManager = CookieSyncManager.createInstance(context)

        val cookieManager = CookieManager.getInstance()

        // Set the Expedia cookies for loading the URL properly
        val cookiesStore = ExpediaServices.getCookies(context)
        cookieManager.setAcceptCookie(true)
        cookieManager.removeSessionCookie()

        if (cookiesStore != null) {
            for (cookies in cookiesStore.values) {
                for (cookie in cookies.values) {
                    cookieManager.setCookie(cookie.domain(), cookie.toString())
                }
            }
        }

        cookieSyncManager.sync()
    }


    init {
        View.inflate(getContext(), R.layout.widget_web_view, this)
        this.orientation = LinearLayout.VERTICAL
        toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
        setToolbarPadding()
      //  loadCookies()
        webView.setWebViewClient(webClient)
        webView.settings.javaScriptEnabled = true
    }

    open var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewURLObservable.subscribe { url ->
            webView.loadUrl(url)
        }
    }

    fun setExitButtonOnClickListener(listener: OnClickListener) {
        toolbar.setNavigationOnClickListener(listener)
    }

    open fun setToolbarPadding() {
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    private fun toggleLoading(loading: Boolean) {
        if (loading) {
            webView.visibility = View.GONE
            progressView.visibility = View.VISIBLE
        } else {
            webView.visibility = View.VISIBLE
            progressView.visibility = View.GONE
        }
    }
}
