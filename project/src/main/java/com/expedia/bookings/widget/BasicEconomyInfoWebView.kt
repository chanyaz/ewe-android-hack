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
        webView.loadData(data.replace("</style>", "body{line-height: 1.5em;color:#767676} ul{padding-left:1.7em;padding-top:0.5em}</style>"), "text/html", "UTF-8")
    }
}
