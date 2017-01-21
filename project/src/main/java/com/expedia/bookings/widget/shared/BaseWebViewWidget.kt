package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel
import okhttp3.Cookie
import okhttp3.HttpUrl
import rx.subjects.PublishSubject
import java.util.*


open class BaseWebViewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val webView: WebView by bindView(R.id.web_view)
    val progressView: ProgressBar by bindView(R.id.webview_progress_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val closeWebView = PublishSubject.create<Unit>()
    val userAccountRefresher: UserAccountRefresher = UserAccountRefresher(context, LineOfBusiness.HOTELS, null)


    var webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            toggleLoading(false)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            toggleLoading(true)
            if (url?.contains("onfirmation")) {
                val cookies = CookieManager.getInstance().getCookie(url)
                addCookies(cookies)
                userAccountRefresher.forceAccountRefresh()
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


    fun addCookies(cookies: String) {
        val services = ExpediaServices(context)

        val EXPEDIA_COOKIES = ArrayList<Cookie>()
        val cookieList = arrayListOf("user","mInfo","lInfo")

        val temp = cookies.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (ar1 in temp) {
            val cookieName = ar1.substring(0, ar1.indexOf("="))
            if(cookieList.contains(cookieName))
            EXPEDIA_COOKIES.add(Cookie.parse(HttpUrl.parse("https://www.expedia.com"), ar1))
        }
        services.mCookieManager.saveFromResponse(HttpUrl.parse("https://www.expedia.com"), EXPEDIA_COOKIES)
    }


    init {
        View.inflate(getContext(), R.layout.widget_web_view, this)
        this.orientation = LinearLayout.VERTICAL
        toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
        setToolbarPadding()
        loadCookies()
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
