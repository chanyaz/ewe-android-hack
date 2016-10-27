package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.shared.AbstractWebViewWidget

class RailSearchLegalInfoWebView(context: Context, attrs: AttributeSet) : AbstractWebViewWidget(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.rail_search_legal_web_view_heading)
    }

    override fun setToolbarPadding() {
        // Do nothing
    }
}
