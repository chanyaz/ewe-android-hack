package com.expedia.bookings.packages.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.extensions.safeSubscribe
import com.expedia.bookings.extensions.subscribeEnabled
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.packages.vm.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.bookings.packages.vm.MultiItemBottomCheckoutContainerViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCostSummaryBreakdownViewModel

class MultiItemBottomCheckoutContainer(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.total_price_widget)
    val checkoutButtonContainer: View by bindView(R.id.button_container)
    val checkoutButton: Button by bindView(R.id.checkout_button)

    var viewModel: MultiItemBottomCheckoutContainerViewModel by notNullAndObservable { vm ->
        vm.resetPriceWidgetObservable.subscribe {
            totalPriceWidget.resetPriceWidget()
            if (totalPriceViewModel.shouldShowTotalPriceLoadingProgress()) {
                vm.checkoutButtonEnableObservable.onNext(false)
            }
        }
        vm.checkoutButtonEnableObservable.subscribeEnabled(checkoutButton)
    }

    var totalPriceViewModel: AbstractUniversalCKOTotalPriceViewModel by notNullAndObservable { vm ->
        totalPriceWidget.viewModel = vm
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView()) {
            vm.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        }
    }

    var baseCostSummaryBreakdownViewModel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        totalPriceWidget.breakdown.viewmodel = vm
        vm.iconVisibilityObservable.safeSubscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    fun toggleCheckoutButton(state: TwoScreenOverviewState) {
        if (state == TwoScreenOverviewState.OTHER) {
            checkoutButtonContainer.visibility = View.GONE
            checkoutButton.visibility = View.GONE
        } else {
            checkoutButtonContainer.visibility = View.VISIBLE
            checkoutButton.visibility = View.VISIBLE
            val checkoutButtonTextColor = ContextCompat.getColor(context, if (state == TwoScreenOverviewState.BUNDLE) {
                R.color.search_dialog_background_v2
            } else {
                R.color.white_disabled
            })
            checkoutButton.setTextColor(checkoutButtonTextColor)
        }
    }

    init {
        View.inflate(context, R.layout.multiitem_bottom_checkout_container, this)
    }
}
