package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.WebViewViewModel

class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    override var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        super.viewModel = vm
        vm as WebCheckoutViewViewModel
        vm.bookedTripIDObservable.subscribe {
            vm.userAccountRefresher.forceAccountRefreshForWebView()
        }

        this.setExitButtonOnClickListener(View.OnClickListener {
            vm.userAccountRefresher.forceAccountRefreshForWebView()
        })
        vm.showProgressBarObservable.subscribe {
            toggleLoading(true)
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = context.getString(R.string.secure_checkout)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        toggleLoading(false)
        if (url.startsWith(PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL)) {
            view.stopLoading()
            (viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.onNext(Uri.parse(url).getQueryParameter("tripid"))
        }
    }

    fun back() {
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }
        (viewModel as WebCheckoutViewViewModel).userAccountRefresher.forceAccountRefreshForWebView()
    }

}