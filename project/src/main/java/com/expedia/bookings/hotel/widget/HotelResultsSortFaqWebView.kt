package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class HotelResultsSortFaqWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {
    val url = PointOfSale.getPointOfSale().hotelsResultsSortFaqUrl

    init {
        webView.settings.domStorageEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.how_we_determine_sort_order)
    }

    fun loadUrl() {
        webView.loadUrl(url)
    }
}
