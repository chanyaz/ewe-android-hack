package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.PackageBreakdownViewModel

public class PackageBundlePriceWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val bundleChevron: ImageView by bindView(R.id.bundle_chevron)
    val bundleTotalPrice: TextView by bindView(R.id.bundle_total_price)
    val bundleSavings: TextView by bindView(R.id.bundle_total_savings)
    val bundleTotalText: TextView by bindView(R.id.bundle_total_text)
    val perPersonText: TextView by bindView(R.id.per_person_text)

    var viewModel: BundlePriceViewModel by notNullAndObservable { vm ->
        vm.totalPriceObservable.subscribeText(bundleTotalPrice)
        vm.savingsPriceObservable.subscribeTextAndVisibility(bundleSavings)
        vm.bundleTextLabelObservable.subscribeText(bundleTotalText)
        vm.perPersonTextLabelObservable.subscribeVisibility(perPersonText)
    }

    val packagebreakdown = PackageBreakDownView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(packagebreakdown)
        builder.setTitle(R.string.cost_summary)
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        builder.create()
    }

    init {
        View.inflate(getContext(), R.layout.bundle_total_price_widget, this)
        rotateChevron(true)

        packagebreakdown.viewmodel = PackageBreakdownViewModel(context)
        packagebreakdown.viewmodel.iconVisibilityObservable.subscribe { show ->
            toggleBundleTotalCompoundDrawable(show)
        }
        bundleTotalText.setOnClickListener {
            // We want to show cost breakdown ONLY in checkout screen. We set the rightDrawable only when createTrip returns. So let's check
            if (bundleTotalText.compoundDrawables[2] != null)
                dialog.show()
        }
    }

    fun rotateChevron(isCollapsed: Boolean) {
        if (isCollapsed) {
            AnimUtils.rotate(bundleChevron)
        } else {
            AnimUtils.reverseRotate(bundleChevron)
        }
    }

    fun toggleBundleTotalCompoundDrawable(show: Boolean) {
        if (show) {
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_checkout_info).mutate()
            bundleTotalText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        } else {
            bundleTotalText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

    }
}
