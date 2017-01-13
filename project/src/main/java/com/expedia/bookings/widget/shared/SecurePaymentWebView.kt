package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel
import com.mobiata.android.Log
import org.apache.http.util.EncodingUtils
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI


class SecurePaymentWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

//    var shouldBeShown = false
    val customWebClient =  object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url?.contains("Confirmation") ?: false) {
                closeWebView.onNext(Unit)
            }
        }
    }
//    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse {
//        if (request?.url.toString().contains("startDate")) {
////            exit the thing
//            return WebResourceResponse("foo", "bar", ByteArrayInputStream(ByteArray(123)))
//        } else {
//            return super.shouldInterceptRequest(view, request)
//        }
//    }
//}
override var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewUrlPostObservable.subscribe { url ->
            webView.loadUrl(url)
        }
}

    init {
        webView.setWebViewClient(customWebClient)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = "3DS Checkout"
    }
}