package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.WebViewViewModel
import com.expedia.vm.flights.FlightCheckoutSummaryViewModel

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs) {
    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)
    val viewModel = FlightCheckoutSummaryViewModel()

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        getCheckoutPresenter().totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.trip_total))
        getCheckoutPresenter().totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
        getCheckoutPresenter().getCheckoutViewModel().showDebitCardsNotAcceptedSubject.subscribe(getCheckoutPresenter().paymentWidget.viewmodel.showDebitCardsNotAcceptedSubject)
        scrollSpaceView = flightSummary.scrollSpaceView
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

    fun resetFlightSummary() {
        flightSummary.collapseFlightWidgets()
    }

    fun getCheckoutPresenter(): FlightCheckoutPresenter {
        return checkoutPresenter as FlightCheckoutPresenter
    }

    override fun trackCheckoutPageLoad() {
        FlightsV2Tracking.trackCheckoutInfoPageLoad()
    }

    override fun trackPaymentCIDLoad() {
        FlightsV2Tracking.trackCheckoutPaymentCID()
    }
}
