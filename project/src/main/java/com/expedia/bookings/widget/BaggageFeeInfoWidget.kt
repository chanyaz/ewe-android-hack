package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaggageFeeInfoViewModel

class BaggageFeeInfoWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val webView: WebView by bindView(R.id.web_view)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }

    init {
        View.inflate(getContext(), R.layout.widget_baggage_fee_info, this)
        this.orientation = LinearLayout.VERTICAL
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    var viewModel: BaggageFeeInfoViewModel by notNullAndObservable { vm ->
        vm.baggageFeeURLObserver.subscribe { url ->
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
