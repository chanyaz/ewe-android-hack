package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class BasicEconomyInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.toolbar_flight_basic_economy_info_title)
    }

    fun loadData(data: String) {
        webView.loadDataWithBaseURL(null, data, "text/html; charset=utf-8", "UTF-8", null)
    }
}
