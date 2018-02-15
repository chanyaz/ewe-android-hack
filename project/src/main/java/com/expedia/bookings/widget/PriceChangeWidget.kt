package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PriceChangeViewModel

class PriceChangeWidget(context: Context, attr: AttributeSet) : LinearLayout(context, attr) {
    val priceChange: TextView by bindView(R.id.price_change_text)

    var viewmodel: PriceChangeViewModel by notNullAndObservable { vm ->
        vm.priceChangeText.subscribeText(priceChange)
        vm.priceChangeDrawable.subscribe { drawable ->
            val icons = priceChange.compoundDrawables
            priceChange.setCompoundDrawablesWithIntrinsicBounds(drawable, icons[1], icons[2], icons[3])
        }
    }

    init {
        View.inflate(context, R.layout.price_change_widget, this)
        orientation = VERTICAL
    }
}
