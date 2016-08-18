package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebViewViewModel

abstract class AbstractWebViewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val webView: WebView by bindView(R.id.web_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }

    init {
        View.inflate(getContext(), R.layout.widget_web_view, this)
        this.orientation = LinearLayout.VERTICAL
    }

    var viewModel: WebViewViewModel by notNullAndObservable { vm ->
        vm.webViewURLObservable.subscribe { url ->
            webView.setWebViewClient(webClient)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
        }
    }

    var webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url)
            return true
        }
    }
}
