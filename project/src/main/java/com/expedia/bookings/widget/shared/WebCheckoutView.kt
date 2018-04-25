package com.expedia.bookings.widget.shared

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.utils.WebViewUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.WebViewViewModel
import com.mobiata.android.util.AndroidUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    val loadingWebview: LinearLayout by bindView(R.id.webview_loading_screen)

    var clearHistory = false

    val chromeClient: WebChromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView, isDialog: Boolean,
                                    isUserGesture: Boolean, resultMsg: Message): Boolean {
            webViewPopUp = WebView(context)
            webViewPopUp!!.run {
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptCanOpenWindowsAutomatically = true
                webViewClient = webClient
                settings.setJavaScriptEnabled(true)
                settings.setSavePassword(false)
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                visibility = View.GONE
            }
            container.addView(webViewPopUp)
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = webViewPopUp
            resultMsg.sendToTarget()

            return true
        }

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
        toolbar.title = context.getString(R.string.secure_checkout)
        toolbar.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        setUserAgentString(AndroidUtils.isTablet(context))
        webView.webChromeClient = chromeClient
    }

    override fun toggleLoading(loading: Boolean) {
        if (ExpediaBookingApp.isInstrumentation()) {
            return
        }
        loadingWebview.setVisibility(loading)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if (urlIsMIDConfirmation(url) || (urlIsLOBBookingConfirmation(url) && isUserBucketedIntoWebCheckout())) {
            view.stopLoading()
            viewModel.showWebViewObservable.onNext(false)
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
        if (webViewPopUp != null) {
            hideWebViewPopUp()
            return
        }

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

    private fun urlIsLOBBookingConfirmation(url: String): Boolean {
        return url.contains(context.getString(R.string.hotel_confirmation_url_tag)) || url.contains(context.getString(R.string.flight_confirmation_url_tag)) || (url.contains(context.getString(R.string.mid_confirmation_url_tag)) ||
                (url.contains(context.getString(R.string.lx_confirmation_url_tag))))
    }

    private fun isUserBucketedIntoWebCheckout(): Boolean {
        return PointOfSale.getPointOfSale().shouldShowWebCheckout() ||
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelsWebCheckout) ||
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout) ||
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppLxWebCheckoutView)
    }

    private fun urlIsMIDConfirmation(url: String): Boolean {
        return isMidAPIEnabled(context) && url.contains(context.getString(R.string.mid_confirmation_url_tag))
    }

    private fun setUserAgentString(isTabletDevice: Boolean) {
        val userAgentString = WebViewUtils.userAgentString
        webView.settings.userAgentString = WebViewUtils.generateUserAgentStringWithDeviceType(userAgentString, isTabletDevice)
    }
}
