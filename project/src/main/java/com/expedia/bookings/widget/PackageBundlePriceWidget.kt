package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BundlePriceViewModel

public class PackageBundlePriceWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val bundleTotalPrice: TextView by bindView(R.id.bundle_total_price)
    val bundleSavings: TextView by bindView(R.id.bundle_total_savings)


    var viewModel: BundlePriceViewModel by notNullAndObservable { vm ->
        vm.totalPriceObservable.subscribeText(bundleTotalPrice)
        vm.savingsPriceObservable.subscribeText(bundleSavings)
    }

    init {
        View.inflate(getContext(), R.layout.bundle_total_price_widget, this)
    }
}
