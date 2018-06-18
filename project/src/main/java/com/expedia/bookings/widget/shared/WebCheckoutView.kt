package com.expedia.bookings.widget.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isShowFlightsNativeRateDetailsWebviewCheckoutEnabled
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.WebViewViewModel
import io.reactivex.subjects.PublishSubject

@Suppress("DEPRECATION")
class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.web_details_loading_overlay)
    val progressIndicatorLayout by bindView<LinearLayout>(R.id.webview_loading_screen)

    val showLoadingIndicator = PublishSubject.create<Boolean>()

    var clearHistory = false

    var checkoutErrorState = false

    override fun chromeClient(): WebChromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView, isDialog: Boolean,
                                    isUserGesture: Boolean, resultMsg: Message): Boolean {
            webViewPopUp = WebView(context)
            webViewPopUp!!.run {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptCanOpenWindowsAutomatically = true
                webViewClient = webClient()
                settings.javaScriptEnabled = true
                settings.savePassword = false
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                visibility = View.GONE
                setDownloadListener { url, _, _, _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    hideWebViewPopUp()
                }
            }
            container.addView(webViewPopUp, 0)
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = webViewPopUp
            resultMsg.sendToTarget()

            return true
        }

        override fun onProgressChanged(view: WebView?, loadProgress: Int) {
            super.onProgressChanged(view, loadProgress)
            if (loadProgress > 33 && loadingOverlay.visibility == View.VISIBLE) {
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
        vm.showWebViewObservable.subscribe { isShown ->
            val window = (context as Activity).window
            if (isShown) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        vm.reloadUrlObservable.subscribe {
            webView.reload()
            webView.clearHistory()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.visibility = View.GONE
        toolbar.title = context.getString(R.string.secure_checkout)
        toolbar.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)

        showLoadingIndicator.subscribe { status ->
            toolbar.setVisibility(!status)
            progressIndicatorLayout.setVisibility(status)
            loadingOverlay.setVisibility(status)
        }
    }

    override fun toggleLoading(loading: Boolean) {
        if (ExpediaBookingApp.isInstrumentation()) {
            return
        }
        showLoadingIndicator.onNext(loading)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        showLoadingIndicator.onNext(true)
        if (checkoutErrorState && this.visibility == View.VISIBLE) {
            view.stopLoading()
            goToSearchAndClearWebView()
        }
        if (shouldShowNativeConfirmation(url)) {
            view.stopLoading()
            viewModel.showWebViewObservable.onNext(false)
            (viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.onNext(Uri.parse(url).getQueryParameter("tripid"))
        } else if (url.contains("CheckoutError")) {
            checkoutErrorState = true
        }
        if (webViewPopUp == null) {
            webView.visibility = View.VISIBLE
        } else {
            webView.visibility = View.GONE
            webViewPopUp!!.visibility = View.VISIBLE
        }
    }

    private fun shouldShowNativeConfirmation(url: String) =
            shouldShowNativePackageConfirmation(url) || shouldShowNativeHotelConfirmation(url) || shouldShowNativeFlightConfirmation(url) || shouldShowNativeLXConfirmation(url)

    override fun setExitButtonOnClickListener(listener: OnClickListener) {
        toolbar.setNavigationOnClickListener {
            viewModel.backObservable.onNext(Unit)
        }
    }

    override fun onPageFinished(url: String) {
        showLoadingIndicator.onNext(false)
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
        if (webViewPopUp != null) {
            hideWebViewPopUp()
            return
        }
        if (checkoutErrorState) {
            goToSearchAndClearWebView()
        }

        if (!previousURLIsAboutBlank() && webView.canGoBack()) {
            webView.goBack()
            return
        }
        (viewModel as WebCheckoutViewViewModel).userAccountRefresher.forceAccountRefreshForWebView()
    }

    @VisibleForTesting
    fun goToSearchAndClearWebView() {
        checkoutErrorState = false
        viewModel.showNativeSearchObservable.onNext(Unit)
        viewModel.webViewURLObservable.onNext("about:blank")
        webView.clearHistory()
        viewModel.showWebViewObservable.onNext(false)
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

    private fun shouldShowNativePackageConfirmation(url: String): Boolean {
        return url.contains(context.getString(R.string.mid_confirmation_url_tag))
    }

    private fun shouldShowNativeHotelConfirmation(url: String): Boolean {
        return url.contains(context.getString(R.string.hotel_confirmation_url_tag)) &&
                (PointOfSale.getPointOfSale().shouldShowWebCheckout() || AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelsWebCheckout))
    }

    private fun shouldShowNativeFlightConfirmation(url: String): Boolean {
        return url.contains(context.getString(R.string.flight_confirmation_url_tag)) &&
                (PointOfSale.getPointOfSale().shouldShowWebCheckout() || isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context))
    }

    private fun shouldShowNativeLXConfirmation(url: String): Boolean {
        return (url.contains(context.getString(R.string.mid_confirmation_url_tag))) &&
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppLxWebCheckoutView)
    }
}
