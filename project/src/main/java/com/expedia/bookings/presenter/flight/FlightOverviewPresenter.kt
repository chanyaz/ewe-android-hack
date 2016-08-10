package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
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
    val airlinesChargePaymentFees = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()

    val AIRLINEFEE_VIEW_TRANSITION_DURATION = 400

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById(R.id.payment_fee_info_webview_stub) as ViewStub
        val airlineFeeWebview = viewStub.inflate() as PaymentFeeInfoWebView
        airlineFeeWebview.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        airlineFeeWebview.toolbar.title = resources.getString(if (airlinesChargePaymentFees) R.string.Airline_fee else R.string.flights_flight_overview_payment_fees)
        airlineFeeWebview.toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        airlineFeeWebview.viewModel = WebViewViewModel()
        airlineFeeWebview
    }

    private val overviewToAirlineFeeWebView = object : Transition(getCheckoutTransitionClass(), PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), AIRLINEFEE_VIEW_TRANSITION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        getCheckoutPresenter().totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.trip_total))
        getCheckoutPresenter().totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
        getCheckoutPresenter().getCheckoutViewModel().showDebitCardsNotAcceptedSubject.subscribe(getCheckoutPresenter().paymentWidget.viewmodel.showDebitCardsNotAcceptedSubject)
        addTransition(overviewToAirlineFeeWebView)
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

        getCheckoutPresenter().cardFeeWarningTextView.setOnClickListener {
            show(paymentFeeInfoWebView)
        }
        getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.subscribe(paymentFeeInfoWebView.viewModel.webViewURLObservable)
        getCheckoutPresenter().getCheckoutViewModel().tripResponseObservable.subscribe {
            flightSummary.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val splitTicketLp = flightSummary.splitTicketInfoContainer.layoutParams
                    splitTicketLp.height = flightSummary.splitTicketInfoContainer.height + getCheckoutPresenter().bottomContainer.height
                    flightSummary.splitTicketInfoContainer.setLayoutParams(splitTicketLp)
                    flightSummary.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    fun getCheckoutPresenter(): FlightCheckoutPresenter {
        return checkoutPresenter as FlightCheckoutPresenter
    }

    override fun getCheckoutTransitionClass(): Class<out Any> {
        return FlightCheckoutPresenter::class.java
    }

    override fun trackCheckoutPageLoad() {
        FlightsV2Tracking.trackCheckoutInfoPageLoad()
    }

    override fun trackPaymentCIDLoad() {
        FlightsV2Tracking.trackCheckoutPaymentCID()
    }
}
