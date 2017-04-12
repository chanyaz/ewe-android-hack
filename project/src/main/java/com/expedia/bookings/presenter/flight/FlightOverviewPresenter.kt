package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.rail.widget.BasicEconomyInfoWebView
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.util.safeSubscribe
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.flights.FlightCheckoutSummaryViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.vm.packages.FlightTotalPriceViewModel
import com.expedia.vm.packages.FlightOverviewSummaryViewModel

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {

    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)
    val viewModel = FlightCheckoutSummaryViewModel()
    val isBucketedForExpandedRateDetailsTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightRateDetailExpansion)
    val isBucketedForShowMoreDetailsOnOverview = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview, R.string.preference_show_more_info_on_flight_overview)
    val showCollapsedToolbar = isBucketedForShowMoreDetailsOnOverview || isBucketedForExpandedRateDetailsTest

    val flightCostSummaryObservable = (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable
    val overviewPageUsableData = PageUsableData()

    private val basicEconomyInfoWebView: BasicEconomyInfoWebView by lazy {
        val viewStub = findViewById(R.id.basic_economy_info_web_view) as ViewStub
        val basicEconomyInfoView = viewStub.inflate() as BasicEconomyInfoWebView
        basicEconomyInfoView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        basicEconomyInfoView
    }

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = FlightCheckoutOverviewViewModel(context)
        totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.trip_total))
        totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
        getCheckoutPresenter().getCheckoutViewModel().showDebitCardsNotAcceptedSubject.subscribe(getCheckoutPresenter().paymentWidget.viewmodel.showDebitCardsNotAcceptedSubject)
        getCheckoutPresenter().getCheckoutViewModel().createTripResponseObservable.safeSubscribe(flightCostSummaryObservable)
        scrollSpaceView = flightSummary.scrollSpaceView
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.visibility = View.INVISIBLE
        bundleOverviewHeader.isExpandable = !showCollapsedToolbar
        val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return bundleOverviewHeader.isExpandable && currentState == BundleDefault::class.java.name
            }
        });
        flightSummary.basicEconomyInfoClickedSubject.subscribe {
            show(basicEconomyInfoWebView)
        }
        flightSummary.viewmodel = FlightOverviewSummaryViewModel(context)
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
        viewModel.showBasicEconomyMessageObservable.subscribeVisibility(flightSummary.basicEconomyMessageTextView)
        viewModel.airlineFeeWarningTextObservable.subscribeText(flightSummary.airlineFeeWarningTextView)
        checkoutPresenter.getCreateTripViewModel().showCreateTripDialogObservable.subscribe {
            show ->
            if (show) {
                flightSummary.basicEconomyMessageTextView.visibility = View.GONE
            } else if (showCollapsedToolbar) {
                bundleOverviewHeader.translateDatesTitleForHeaderToolbar()
            }
        }
        addTransition(overviewToBasicEconomyInfoWebView)
        if (isBucketedForShowMoreDetailsOnOverview) {
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.subTitleText.filter { Strings.isNotEmpty(it) }.subscribe {
                bundleOverviewHeader.checkoutOverviewHeaderToolbar.checkInOutDates.text = it
            }
        }
    }

    val overviewToBasicEconomyInfoWebView = object : VisibilityTransition(this, BaseTwoScreenOverviewPresenter.BundleDefault::class.java, BasicEconomyInfoWebView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            basicEconomyInfoWebView.visibility = if (forward) View.VISIBLE else View.GONE
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
        checkoutPresenter.slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(!showCollapsedToolbar)
            toggleCheckoutButtonAndSliderVisibility(true)
        }
    }

    override fun translateHeader(f: Float, forward: Boolean) {
        if (!showCollapsedToolbar)
            super.translateHeader(f, forward)
    }

    override fun getCostSummaryBreakdownViewModel(): FlightCostSummaryBreakdownViewModel {
        return FlightCostSummaryBreakdownViewModel(context)
    }

    override fun onTripResponse(tripResponse: TripResponse?) {
        tripResponse as FlightTripResponse
        totalPriceWidget.viewModel.total.onNext(tripResponse.newPrice())
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(tripResponse)
        viewModel.showBasicEconomyMessageObservable.onNext(shouldShowBasicEconomyMessage(tripResponse))
        basicEconomyInfoWebView.loadData(tripResponse.details.basicEconomyFareRules)
        overviewPageUsableData.markAllViewsLoaded(System.currentTimeMillis())
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as FlightCreateTripResponse
        val flightSearchParams = Db.getFlightSearchParams()
        FlightsV2Tracking.trackShowFlightOverView(flightSearchParams, createTripResponse, overviewPageUsableData)
    }

    override fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel {
        return FlightTotalPriceViewModel(context)
    }

    private fun shouldShowBasicEconomyMessage(tripResponse: FlightTripResponse?): Boolean {
        if (tripResponse != null) {
            return tripResponse.details.legs?.filter { it.isBasicEconomy }?.any() ?: false
        }
        return false
    }
}
