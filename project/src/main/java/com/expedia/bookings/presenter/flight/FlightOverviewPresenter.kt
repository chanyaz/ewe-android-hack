package com.expedia.bookings.presenter.flight

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightCheckoutPresenter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.flights.FlightOverviewViewModel

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs) {
    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)
    val viewModel = FlightOverviewViewModel()

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
    }

    override fun inflate() {
        View.inflate(context, R.layout.flight_overview, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(flightSummary)
        bundleOverviewHeader.nestedScrollView.addView(flightSummary)

        viewModel.showFreeCancellationObservable.subscribeVisibility(flightSummary.freeCancellationLabelTextView)
        viewModel.showSplitTicketMessagingObservable.subscribeVisibility(flightSummary.splitTicketInfoContainer)
        viewModel.splitTicketBaggageFeesLinksObservable.subscribeText(flightSummary.splitTicketBaggageFeesTextView)
    }

    fun getCheckoutPresenter() : FlightCheckoutPresenter {
        return checkoutPresenter as FlightCheckoutPresenter
    }

    override fun getCheckoutTransitionClass() : Class<out Any> {
        return FlightCheckoutPresenter::class.java
    }

    override fun trackCheckoutPageLoad() {
    }

    override fun trackPaymentCIDLoad() {
    }
}