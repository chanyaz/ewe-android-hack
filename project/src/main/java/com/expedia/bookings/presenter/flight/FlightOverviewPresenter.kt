package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightCheckoutPresenter
import com.expedia.vm.FlightCheckoutOverviewViewModel

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs) {

    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)

    override fun inflate() {
        View.inflate(context, R.layout.flight_overview, this)
    }

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(flightSummary)
        bundleOverviewHeader.nestedScrollView.addView(flightSummary)
    }

    fun getCheckoutPresenter() : FlightCheckoutPresenter {
        return checkoutPresenter as FlightCheckoutPresenter
    }

    override fun getCheckoutTransitionClass() : Class<out Any> {
        return FlightCheckoutPresenter::class.java
    }
}