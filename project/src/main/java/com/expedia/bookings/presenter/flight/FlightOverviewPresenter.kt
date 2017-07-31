package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.rail.widget.BasicEconomyInfoWebView
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FareFamilyCardView
import com.expedia.bookings.widget.InsuranceWidget
import com.expedia.bookings.widget.flights.FlightFareFamilyWidget
import com.expedia.util.safeSubscribe
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FareFamilyViewModel
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.InsuranceViewModel
import com.expedia.vm.flights.FlightCheckoutSummaryViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightFareFamilyViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.vm.packages.FlightTotalPriceViewModel
import com.expedia.vm.packages.FlightOverviewSummaryViewModel
import javax.inject.Inject

class FlightOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {

    lateinit var insuranceServices: InsuranceServices
        @Inject set

    val flightSummary: FlightSummaryWidget by bindView(R.id.flight_summary)
    val viewModel = FlightCheckoutSummaryViewModel()
    val isBucketedForShowMoreDetailsOnOverview = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview)
    val showCollapsedToolbar = isBucketedForShowMoreDetailsOnOverview

    val flightCostSummaryObservable = (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable
    val overviewPageUsableData = PageUsableData()
    val isUserBucketedForFareFamily = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFareFamilyFlightSummary,
            R.string.preference_fare_family_flight_summary)


    private val basicEconomyInfoWebView: BasicEconomyInfoWebView by lazy {
        val viewStub = findViewById(R.id.basic_economy_info_web_view) as ViewStub
        val basicEconomyInfoView = viewStub.inflate() as BasicEconomyInfoWebView
        basicEconomyInfoView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        basicEconomyInfoView
    }

    val flightFareFamilyDetailsWidget: FlightFareFamilyWidget by lazy {
        val viewStub = findViewById(R.id.fare_family_details_view) as ViewStub
        val flightFareFamilyView = viewStub.inflate() as FlightFareFamilyWidget
        flightFareFamilyView.viewModel = FlightFareFamilyViewModel(context)
        flightFareFamilyView
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

    val fareFamilyCardView: FareFamilyCardView by lazy {
        val widget = findViewById(R.id.fare_family_widget) as FareFamilyCardView
        widget.viewModel = FareFamilyViewModel(context)
        widget.viewModel.fareFamilyCardClickObserver.subscribe {
            flightFareFamilyDetailsWidget.viewModel.showFareFamilyObservable.onNext(Unit)
            show(flightFareFamilyDetailsWidget)
        }
        widget
    }

    val insuranceWidget: InsuranceWidget by lazy {
        val widget = findViewById(R.id.insurance_widget) as InsuranceWidget
        widget.viewModel = InsuranceViewModel(context, insuranceServices)
        widget.viewModel.updatedTripObservable.subscribe(checkoutPresenter.getCreateTripViewModel().createTripResponseObservable)
        widget
    }

    override fun inflate() {
        View.inflate(context, R.layout.flight_overview, this)
    }

    override fun injectComponents() {
        Ui.getApplication(context).flightComponent().inject(this)
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
        addTransition(overviewToFamilyFare)
        if (isBucketedForShowMoreDetailsOnOverview) {
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.subTitleText.filter { Strings.isNotEmpty(it) }.subscribe {
                bundleOverviewHeader.checkoutOverviewHeaderToolbar.checkInOutDates.text = it
            }
        }
    }

    override val defaultTransition = object : TwoScreenOverviewDefaultTransition() {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            val offerInsuranceInFlightSummary = Db.getAbacusResponse().isUserBucketedForTest(
                    AbacusUtils.EBAndroidAppOfferInsuranceInFlightSummary)
            insuranceWidget.viewModel.widgetVisibilityAllowedObservable.onNext(offerInsuranceInFlightSummary)
        }
    }

    val overviewToBasicEconomyInfoWebView = object : VisibilityTransition(this, BaseTwoScreenOverviewPresenter.BundleDefault::class.java, BasicEconomyInfoWebView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            basicEconomyInfoWebView.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    val overviewToFamilyFare = object : Presenter.Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, FlightFareFamilyWidget::class.java, DecelerateInterpolator(2f), 500) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            flightFareFamilyDetailsWidget.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1f - f else f
            flightFareFamilyDetailsWidget.translationY = flightFareFamilyDetailsWidget.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                flightFareFamilyDetailsWidget.visibility = View.VISIBLE
                flightFareFamilyDetailsWidget.translationY = 0f
            } else {
                flightFareFamilyDetailsWidget.visibility = View.GONE
                flightFareFamilyDetailsWidget.translationY = (flightFareFamilyDetailsWidget.height).toFloat()
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
        bottomCheckoutContainer.slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(!showCollapsedToolbar)
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
        insuranceWidget.viewModel.tripObservable.onNext(tripResponse)
        if (isUserBucketedForFareFamily) {
            fareFamilyCardView.viewModel.tripObservable.onNext(tripResponse)
            flightFareFamilyDetailsWidget.viewModel.tripObservable.onNext(tripResponse)
        }
        viewModel.showBasicEconomyMessageObservable.onNext(shouldShowBasicEconomyMessage(tripResponse))
        basicEconomyInfoWebView.loadData(tripResponse.details.basicEconomyFareRules)
        overviewPageUsableData.markAllViewsLoaded(System.currentTimeMillis())
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as FlightCreateTripResponse
        val flightSearchParams = Db.getFlightSearchParams()
        FlightsV2Tracking.trackShowFlightOverView(flightSearchParams, createTripResponse, overviewPageUsableData,
                viewModel.outboundSelectedAndTotalLegRank, viewModel.inboundSelectedAndTotalLegRank)
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
