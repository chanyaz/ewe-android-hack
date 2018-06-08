package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingBundleView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var viewModel: IHotelItinPricingBundleDescriptionViewModel by notNullAndObservable {
        it.bundleContainerViewVisibilitySubject.subscribeVisibility(this)
        it.bundleContainerResetSubject.subscribe {
            this.removeAllViews()
        }
        it.bundleProductDescriptionSubject.subscribe { item ->
            val view: BundlePriceDescriptionItemView = Ui.inflate(R.layout.hotel_itin_price_bundle_item_view, this, false)
            view.text = item
            this.addView(view)
        }
    }
}

class BundlePriceDescriptionItemView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs)
