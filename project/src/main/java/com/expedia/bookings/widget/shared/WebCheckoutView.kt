package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.updateVisibility
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.WebViewViewModel

class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    val loadingWebview: LinearLayout by bindView(R.id.webview_loading_screen)

    var clearHistory = false

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
    }

    override fun toggleLoading(loading: Boolean) {
        if (ExpediaBookingApp.isAutomation()) {
            return
        }
        loadingWebview.updateVisibility(loading)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        toggleLoading(false)
        if (urlHasPOSWebBookingConfirmationUrl(url)) {
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
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }
        (viewModel as WebCheckoutViewViewModel).userAccountRefresher.forceAccountRefreshForWebView()
    }

    fun clearHistory() {
        clearHistory = true
    }

    private fun urlHasPOSWebBookingConfirmationUrl(url: String): Boolean {
        return (!PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL.isNullOrBlank()
                && url.startsWith(PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL)) ||
                (!PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL.isNullOrBlank()
                && url.startsWith(PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL))
    }
}
