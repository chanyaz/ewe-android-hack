package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailSummaryWidget
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailSummaryViewModel

class RailTripOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs) {
    val railTripSummary: RailSummaryWidget by bindView(R.id.rail_summary)

    override fun inflate() {
        View.inflate(context, R.layout.rail_overview, this)
    }

    init {
        val overviewVM = RailCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = overviewVM
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = overviewVM
        railTripSummary.viewModel = RailSummaryViewModel(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(railTripSummary)
        bundleOverviewHeader.nestedScrollView.addView(railTripSummary)
    }

    fun getCheckoutPresenter(): RailCheckoutPresenter {
        return checkoutPresenter as RailCheckoutPresenter
    }

    override fun trackCheckoutPageLoad() {
    }

    override fun trackPaymentCIDLoad() {
    }

}