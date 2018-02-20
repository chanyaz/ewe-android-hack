package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.bookings.widget.SlidingBundleWidgetListener
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.BundleTotalPriceTopWidget
import com.expedia.bookings.widget.packages.PackageFlightListAdapter
import com.expedia.util.Optional
import com.expedia.util.endlessObserver
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.PackageToolbarViewModel
import com.expedia.vm.packages.FlightOverviewViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class PackageFlightPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    val bundleSlidingWidget: SlidingBundleWidget by bindView(R.id.sliding_bundle_widget)
    lateinit var slidingBundleWidgetListener: SlidingBundleWidgetListener

    val bundlePriceWidgetTop: BundleTotalPriceTopWidget by lazy {
        val viewStub = findViewById<ViewStub>(R.id.bundle_total_top_stub)
        viewStub.inflate() as BundleTotalPriceTopWidget
    }

    val bundlePriceWidgetTopShadow: View by lazy {
        findViewById<View>(R.id.toolbar_dropshadow_bundle_total_top)
    }

    override val overviewTransition = object : OverviewTransition(this) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            disableSlidingWidget(forward)
            if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                bundlePriceWidgetTop.isClickable = !forward
            }
        }
    }

    private val flightOverviewSelected = endlessObserver<FlightLeg> { flight ->
        val params = Db.sharedInstance.packageParams
        val packageResponse = Db.getPackageResponse()

        if (flight.outbound) {
            Db.setPackageSelectedOutboundFlight(flight)
            params.currentFlights[0] = flight.legId
        } else {
            Db.setPackageFlightBundle(Db.sharedInstance.packageSelectedOutboundFlight, flight)
            params.currentFlights[1] = flight.legId
            params.latestSelectedOfferInfo.flightPIID = packageResponse.getSelectedFlightPIID(params.currentFlights[0], params.currentFlights[1])

            params.latestSelectedOfferInfo.inboundFlightBaggageFeesUrl = flight.baggageFeesUrl
            params.latestSelectedOfferInfo.outboundFlightBaggageFeesUrl = packageResponse.getFlightLegs().firstOrNull { it.legId == params.currentFlights[0] }?.baggageFeesUrl
            params.latestSelectedOfferInfo.isSplitTicketFlights = packageResponse.isSplitTicketFlights(params.currentFlights[0], params.currentFlights[1])
            params.latestSelectedOfferInfo.hotelCheckInDate = packageResponse.getHotelCheckInDate()
            params.latestSelectedOfferInfo.hotelCheckOutDate = packageResponse.getHotelCheckOutDate()
            params.latestSelectedOfferInfo.ratePlanCode = packageResponse.getRatePlanCode()
            params.latestSelectedOfferInfo.roomTypeCode = packageResponse.getRoomTypeCode()
        }
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid
        params.latestSelectedOfferInfo.productOfferPrice = flight.packageOfferModel.price
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_FLIGHT)
        packageResponse.setCurrentOfferPrice(flight.packageOfferModel.price)

        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    init {
        toolbarViewModel = PackageToolbarViewModel(context)
        toolbarViewModel.menuVisibilitySubject.subscribe { showMenu ->
            if (!isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                menuFilter.isVisible = showMenu
            }
        }

        View.inflate(getContext(), R.layout.package_flight_presenter, this)
        resultsPresenter.showFilterButton = isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            val params = Db.sharedInstance.packageParams
            params.selectedLegId = null
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE, isMidAPIEnabled(context)))
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE, isMidAPIEnabled(context)))
        }

        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_FLIGHT)
        val isOutboundSearch = Db.sharedInstance.packageParams?.isOutboundSearch(isMidAPIEnabled(context)) ?: false
        val bestPlusAllFlights = Db.getPackageResponse().getFlightLegs().filter { it.outbound == isOutboundSearch && it.packageOfferModel != null }

        // move bestFlight to the first place of the list
        val bestFlight = bestPlusAllFlights.find { it.isBestFlight }
        val allFlights = bestPlusAllFlights.filterNot { it.isBestFlight }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount }.toMutableList()

        if (bestFlight != null) {
            allFlights.add(0, bestFlight)
        }
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.sharedInstance.packageParams.isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)

        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        resultsPresenter.resultsViewModel.flightResultsObservable.onNext(allFlights)

        if (!isOutboundResultsPresenter() && Db.sharedInstance.packageSelectedOutboundFlight != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.sharedInstance.packageSelectedOutboundFlight)
        }
        val numTravelers = Db.sharedInstance.packageParams.guests
        overviewPresenter.vm.selectedFlightLegSubject.subscribe { selectedFlight ->
            if (selectedFlight.outbound) {
                PackagesPageUsableData.FLIGHT_OUTBOUND_DETAILS.pageUsableData.markPageLoadStarted()
            } else {
                PackagesPageUsableData.FLIGHT_INBOUND_DETAILS.pageUsableData.markPageLoadStarted()
            }

            overviewPresenter.paymentFeesMayApplyTextView.setOnClickListener {
                if (!selectedFlight.airlineMessageModel?.airlineFeeLink.isNullOrBlank()) {
                    overviewPresenter.showPaymentFeesObservable.onNext(true)
                } else {
                    overviewPresenter.paymentFeesMayApplyTextView.background = null
                    overviewPresenter.showPaymentFeesObservable.onNext(false)
                }
            }
        }
        overviewPresenter.vm.numberOfTravelers.onNext(numTravelers)
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOverviewSelected)
        val destinationOrOrigin = if (isOutboundResultsPresenter()) Db.sharedInstance.packageParams.destination else Db.sharedInstance.packageParams.origin
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        toolbarViewModel.regionNames.onNext(Optional(destinationOrOrigin?.regionNames))
        toolbarViewModel.country.onNext(Optional(destinationOrOrigin?.hierarchyInfo?.country?.name))
        toolbarViewModel.airport.onNext(Optional(destinationOrOrigin?.hierarchyInfo?.airport?.airportCode))
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundResultsPresenter()) Db.sharedInstance.packageParams.startDate else Db.sharedInstance.packageParams.endDate as LocalDate)
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView()) {
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
            bundleSlidingWidget.bundlePriceFooter.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        }
        overviewPresenter.vm.obFeeDetailsUrlObservable.subscribe(paymentFeeInfoWebView.viewModel.webViewURLObservable)
        Db.sharedInstance.packageParams.flightLegList = allFlights
        trackFlightResultsLoad()
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            bundlePriceWidgetTopShadow.visibility = View.VISIBLE
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text_new))
            bundlePriceWidgetTop.setOnClickListener {
                PackagesTracking().trackBundleWidgetTap()
                show(bundleSlidingWidget)
            }
            bundleSlidingWidget.bundlePriceWidget.viewModel.perPersonTextLabelObservable.subscribeVisibility(bundlePriceWidgetTop.bundlePerPersonText)
            bundleSlidingWidget.bundlePriceFooter.viewModel.totalPriceObservable.subscribeTextAndVisibility(bundlePriceWidgetTop.bundleTotalPrice)
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.subscribeText(bundlePriceWidgetTop.bundleTitleText)
        }
    }

    private fun setupMenuFilter() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        val filterCountText = toolbarFilterItemActionView.findViewById<TextView>(R.id.filter_count_text)
        val filterPlaceholderImageView = toolbarFilterItemActionView.findViewById<ImageView>(R.id.filter_placeholder_icon)
        val filterButtonText = toolbarFilterItemActionView.findViewById<TextView>(R.id.filter_text)
        val filterBtn = toolbarFilterItemActionView.findViewById<LinearLayout>(R.id.filter_btn)
        toolbarFilterItemActionView.setOnLongClickListener {
            val size = Point()
            display.getSize(size)
            val width = size.x
            val toast = Toast.makeText(context, context.getString(R.string.sort_and_filter), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, width - filterBtn.width, filterBtn.height)
            toast.show()
            true
        }

        filterButtonText.visibility = GONE
        filterBtn.setOnClickListener { show(filter) }

        if (!isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            menuFilter.actionView = toolbarFilterItemActionView
        }
        filter.viewModelBase.filterCountObservable.map { it.toString() }.subscribeText(filterCountText)
        filter.viewModelBase.filterCountObservable.map {
            Phrase.from(resources.getQuantityString(R.plurals.no_of_filters_applied_TEMPLATE, it))
                    .put("filterno", it)
                    .format().toString()
        }.subscribeContentDescription(filterCountText)
        filter.viewModelBase.filterCountObservable.map { it > 0 }.subscribeVisibility(filterCountText)
        filter.viewModelBase.filterCountObservable.map { it > 0 }.subscribeInverseVisibility(filterPlaceholderImageView)
    }

    override fun addResultOverViewTransition() {
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.sharedInstance.packageSelectedOutboundFlight)
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.getPackageFlightBundle().second)
        } else {
            super.addResultOverViewTransition()
            show(resultsPresenter)
        }
    }

    override fun makeFlightOverviewModel(): AbstractFlightOverviewViewModel {
        return FlightOverviewViewModel(context)
    }

    private fun addBackFlowTransition() {
        addDefaultTransition(backFlowDefaultTransition)
        addTransition(backFlowOverviewTransition)
        show(resultsPresenter)
        show(overviewPresenter)
    }

    override fun isOutboundResultsPresenter(): Boolean = Db.sharedInstance.packageParams?.isOutboundSearch(isMidAPIEnabled(context)) ?: false

    override fun trackFlightOverviewLoad(flight: FlightLeg) {
        val isOutboundSearch = Db.sharedInstance.packageParams?.isOutboundSearch(isMidAPIEnabled(context)) ?: false

        if (isOutboundSearch) {
            PackagesPageUsableData.FLIGHT_OUTBOUND_DETAILS.pageUsableData.markAllViewsLoaded()
        } else {
            PackagesPageUsableData.FLIGHT_INBOUND_DETAILS.pageUsableData.markAllViewsLoaded()
        }

        PackagesTracking().trackFlightRoundTripDetailsLoad(isOutboundSearch,
                if (isOutboundSearch) PackagesPageUsableData.FLIGHT_OUTBOUND_DETAILS.pageUsableData else PackagesPageUsableData.FLIGHT_INBOUND_DETAILS.pageUsableData,
                flight)
    }

    override fun trackFlightSortFilterLoad() {
        PackagesTracking().trackFlightSortFilterLoad()
    }

    override fun trackFlightResultsLoad() {
        val isOutboundSearch = Db.sharedInstance.packageParams?.isOutboundSearch(isMidAPIEnabled(context)) ?: false
        PackagesTracking().trackFlightRoundTripLoad(isOutboundSearch, Db.sharedInstance.packageParams, if (isOutboundSearch) PackagesPageUsableData.FLIGHT_OUTBOUND.pageUsableData else PackagesPageUsableData.FLIGHT_INBOUND.pageUsableData)
    }

    private val backFlowDefaultTransition = object : DefaultTransition(FlightResultsListViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            filter.visibility = View.GONE
            resultsPresenter.visibility = View.INVISIBLE
            overviewPresenter.visibility = View.VISIBLE
        }
    }

    private val backFlowOverviewTransition = object : Transition(FlightResultsListViewPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(!forward)
            overviewPresenter.visibility = if (forward) View.VISIBLE else View.INVISIBLE
            resultsPresenter.visibility = if (forward) View.INVISIBLE else View.VISIBLE
            viewBundleSetVisibility(!forward)
            if (!forward) {
                trackFlightResultsLoad()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        setupMenuFilter()

        addTransition(resultsToOverview)
        bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.rowContainer.setOnClickListener {
            if (isShowingBundle()) {
                val outBoundFlightWidget = bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget
                if (isOutboundResultsPresenter()) {
                    backToOutboundResults()
                } else {
                    if (!outBoundFlightWidget.isFlightSegmentDetailsExpanded()) {
                        outBoundFlightWidget.expandFlightDetails()
                    } else {
                        outBoundFlightWidget.collapseFlightDetails(true)
                    }
                }
            }
        }
        bundleSlidingWidget.bundleOverViewWidget.inboundFlightWidget.rowContainer.setOnClickListener {
            if (isShowingBundle()) {
                back()
            }
        }
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
            PackagesTracking().trackBundleWidgetTap()
            show(bundleSlidingWidget)
        }

        slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this)
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)

        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            resultsPresenter.dockedOutboundFlightSelection.setBackgroundColor(ContextCompat.getColor(context, R.color.docketOutboundWidgetGray))
            resultsPresenter.dockedOutboundFlightShadow.layoutParams.height = resources.getDimension(R.dimen.package_docked_outbound_view_seperator).toInt()
        }
    }

    fun updateOverviewAnimationDuration(duration: Int) {
        resultsToOverview.animationDuration = duration
    }

    fun isShowingBundle(): Boolean {
        val isShowingBundle = Strings.equals(currentState, SlidingBundleWidget::class.java.name)
        return isShowingBundle
    }

    override fun viewBundleSetVisibility(forward: Boolean) {
        bundleSlidingWidget.visibility = if (forward) View.VISIBLE else View.GONE
    }

    private val resultsToOverview = object : Transition(FlightResultsListViewPresenter::class.java.name, SlidingBundleWidget::class.java.name, AccelerateDecelerateInterpolator(), bundleSlidingWidget.REGULAR_ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            resultsPresenter.recyclerView.isEnabled = !forward
            bundleSlidingWidget.startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleSlidingWidget.updateBundleTransition(f, forward)
        }

        override fun endTransition(forward: Boolean) {
            bundleSlidingWidget.finalizeBundleTransition(forward)
        }
    }

    override fun setupToolbarMenu() {
        if (!isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            toolbar.inflateMenu(R.menu.package_flights_menu)
        }
    }

    override fun trackShowBaggageFee() = PackagesTracking().trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() {
        // do nothing. Not applicable to Packages LOB
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun disableSlidingWidget(isDisabled: Boolean) {
        bundleSlidingWidget.bundlePriceWidget.isClickable = !isDisabled
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(if (isDisabled) null else slidingBundleWidgetListener.onTouchListener)
    }
}
