package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.WebViewUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.util.notNullAndObservable
import com.expedia.util.updateVisibility
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.WebViewViewModel
import com.mobiata.android.util.AndroidUtils

class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    val loadingWebview: LinearLayout by bindView(R.id.webview_loading_screen)

    var clearHistory = false

    val chromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, loadProgress: Int) {
            super.onProgressChanged(view, loadProgress)
            if (loadProgress > 33 && loadingWebview.visibility == View.VISIBLE) {
                toggleLoading(false)
            }
        }
    }

    override var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        super.viewModel = vm
        vm as WebCheckoutViewViewModel
        vm.bookedTripIDObservable.subscribe {
            vm.userAccountRefresher.forceAccountRefreshForWebView()
        }

        this.setExitButtonOnClickListener(View.OnClickListener {
            vm.userAccountRefresher.forceAccountRefreshForWebView()
        })
        vm.showLoadingObservable.subscribe {
            toggleLoading(true)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = context.getString(R.string.secure_checkout)
        toolbar.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        setUserAgentString(AndroidUtils.isTablet(context))
        webView.webChromeClient = chromeClient
    }

    override fun toggleLoading(loading: Boolean) {
        if (ExpediaBookingApp.isAutomation()) {
            return
        }
        loadingWebview.updateVisibility(loading)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if (urlHasPOSWebBookingConfirmationUrl(url) || urlIsMIDConfirmation(url)) {
            view.stopLoading()
            (viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.onNext(Uri.parse(url).getQueryParameter("tripid"))
        }
    }

    override fun setExitButtonOnClickListener(listener: OnClickListener) {
        toolbar.setNavigationOnClickListener {
            viewModel.backObservable.onNext(Unit)
        }
    }

    override fun onPageFinished(url: String) {
        super.onPageFinished(url)
        if (clearHistory) {
            webView.clearHistory()
            clearHistory = false
        }
        if (url.contains("about:blank")) {
            viewModel.blankViewObservable.onNext(Unit)
        }
    }

    fun back() {
        if (!previousURLIsAboutBlank() && webView.canGoBack()) {
            webView.goBack()
            return
        }
        (viewModel as WebCheckoutViewViewModel).userAccountRefresher.forceAccountRefreshForWebView()
    }

    fun clearHistory() {
        clearHistory = true
    }

    private fun previousURLIsAboutBlank(): Boolean {
        val copyBackForwardList = webView.copyBackForwardList()
        copyBackForwardList?.let {
            val currentURLIndex = copyBackForwardList.currentIndex
            if (currentURLIndex > 0) {
                val previousIndex = currentURLIndex - 1
                val previousItem = copyBackForwardList.getItemAtIndex(previousIndex)
                return previousItem?.url?.contains("about:blank") ?: false
            }
        }
        return false
    }

    private fun urlHasPOSWebBookingConfirmationUrl(url: String): Boolean {
        return (!PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL.isNullOrBlank()
                && url.startsWith(PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL)) ||
                (!PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL.isNullOrBlank()
                && url.startsWith(PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL))
    }

    private fun urlIsMIDConfirmation(url: String): Boolean {
        return isMidAPIEnabled(context) && url.contains(context.getString(R.string.mid_confirmation_url_tag))
    }

    private fun setUserAgentString(isTabletDevice: Boolean) {
        val userAgentString = WebViewUtils.userAgentString
        webView.settings.userAgentString = WebViewUtils.generateUserAgentStringWithDeviceType(userAgentString, isTabletDevice)
    }
}
