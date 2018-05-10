package com.expedia.bookings.packages.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class BundleTotalPriceTopWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val bundleTitleText by bindView<TextView>(R.id.view_bundle_title)
    val bundleInfoIcon by bindView<ImageView>(R.id.sliding_bundle_info_icon)
    val bundleTotalPrice by bindView<TextView>(R.id.bundle_total_price)
    val bundlePerPersonText by bindView<TextView>(R.id.per_person_text)

    init {
        View.inflate(context, R.layout.bundle_price_top, this)
    }
}
