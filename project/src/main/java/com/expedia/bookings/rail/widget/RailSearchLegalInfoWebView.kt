package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class RailSearchLegalInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {
    val url = PointOfSale.getPointOfSale().railsPaymentAndTicketDeliveryFeesUrl

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.rail_search_legal_web_view_heading)
    }

    fun loadUrl() {
        webView.loadUrl(url)
    }
}
