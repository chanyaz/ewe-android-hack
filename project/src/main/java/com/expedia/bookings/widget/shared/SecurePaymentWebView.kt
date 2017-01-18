package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel


class SecurePaymentWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    override var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewUrlPostObservable.subscribe { url ->
            webView.loadUrl(url)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = "Web Checkout for 3DS POS"
    }
}