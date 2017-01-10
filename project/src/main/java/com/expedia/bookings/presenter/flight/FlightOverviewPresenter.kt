package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.safeSubscribe
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.flights.FlightCheckoutSummaryViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.traveler.FlightTravelersViewModel

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {
    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)
    val viewModel = FlightCheckoutSummaryViewModel()
    val isBucketedForExpandedRateDetailsTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightRateDetailExpansion)

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.trip_total))
        totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
        getCheckoutPresenter().getCheckoutViewModel().showDebitCardsNotAcceptedSubject.subscribe(getCheckoutPresenter().paymentWidget.viewmodel.showDebitCardsNotAcceptedSubject)
        scrollSpaceView = flightSummary.scrollSpaceView
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.visibility = View.INVISIBLE
        bundleOverviewHeader.isExpandable = !isBucketedForExpandedRateDetailsTest
        val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return bundleOverviewHeader.isExpandable && currentState == BundleDefault::class.java.name
            }
        });
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
        viewModel.showAirlineFeeWarningObservable.subscribeVisibility(flightSummary.airlineFeeWarningTextView)
        viewModel.airlineFeeWarningTextObservable.subscribeText(flightSummary.airlineFeeWarningTextView)
        checkoutPresenter.getCreateTripViewModel().showCreateTripDialogObservable.subscribe {
            show ->
            if (!show && isBucketedForExpandedRateDetailsTest) {
                bundleOverviewHeader.translateDatesTitleForHeaderToolbar()
            }
        }
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

    override fun resetCheckoutState() {
        slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(!isBucketedForExpandedRateDetailsTest)
            toggleCheckoutButtonAndSliderVisibility(true)
        }
    }

    override fun translateHeader(f: Float, forward: Boolean) {
        if (!isBucketedForExpandedRateDetailsTest)
            super.translateHeader(f, forward)
    }

    override fun getCostSummaryBreakdownViewModel(): FlightCostSummaryBreakdownViewModel {
        return FlightCostSummaryBreakdownViewModel(context)
    }

    override fun onCreateTripResponse(tripResponse: TripResponse?) {
        onTripResponse(tripResponse)
    }

    private fun onTripResponse(tripResponse: TripResponse?) {
        tripResponse as FlightTripResponse
        getCheckoutPresenter().onCreateTripResponse(tripResponse)
        totalPriceWidget.viewModel.total.onNext(tripResponse.newPrice())
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (checkoutPresenter.travelersPresenter.viewModel as FlightTravelersViewModel).flightOfferObservable.onNext(tripResponse.details.offer)
        (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(tripResponse)
    }

    override fun handlePriceChange(tripResponse: TripResponse) {
        tripResponse as FlightCheckoutResponse
        val newPrice = tripResponse.newPrice()
        val oldPrice = tripResponse.getOldPrice()
        if (oldPrice != null) {
            priceChangeWidget.viewmodel.originalPrice.onNext(oldPrice)
            priceChangeWidget.viewmodel.newPrice.onNext(newPrice)
        }
        onTripResponse(tripResponse)
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as FlightCreateTripResponse
        val flightSearchParams = Db.getFlightSearchParams()
        FlightsV2Tracking.trackShowFlightOverView(flightSearchParams, createTripResponse)
    }
}
