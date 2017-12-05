package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.bookings.widget.SlidingBundleWidgetListener
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.PackageFlightListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.packages.FlightOverviewViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class PackageFlightPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    val bundleSlidingWidget: SlidingBundleWidget by bindView(R.id.sliding_bundle_widget)
    lateinit var slidingBundleWidgetListener: SlidingBundleWidgetListener

    override val overviewTransition = object : OverviewTransition(this) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            disableSlidingWidget(forward)
        }
    }

    private val flightOverviewSelected = endlessObserver<FlightLeg> { flight ->
        val params = Db.getPackageParams()
        if (flight.outbound) {
            Db.setPackageSelectedOutboundFlight(flight)
            params.currentFlights[0] = flight.legId
        } else {
            Db.setPackageFlightBundle(Db.getPackageSelectedOutboundFlight(), flight)
            params.currentFlights[1] = flight.legId
            params.latestSelectedFlightPIID = Db.getPackageResponse().getSelectedFlightPIID(params.currentFlights[0], params.currentFlights[1])
        }
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid
        params.latestSelectedProductOfferPrice = flight.packageOfferModel.price
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_FLIGHT)
        val response = Db.getPackageResponse()
        response.setCurrentOfferPrice(flight.packageOfferModel.price)

        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    init {
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
            val params = Db.getPackageParams()
            params.selectedLegId = null
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE, isMidAPIEnabled(context)))
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE, isMidAPIEnabled(context)))
        }

        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_FLIGHT)
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch(isMidAPIEnabled(context)) ?: false
        val bestPlusAllFlights = Db.getPackageResponse().getFlightLegs().filter { it.outbound == isOutboundSearch && it.packageOfferModel != null }

        // move bestFlight to the first place of the list
        val bestFlight = bestPlusAllFlights.find { it.isBestFlight }
        val allFlights = bestPlusAllFlights.filterNot { it.isBestFlight }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount }.toMutableList()

        if (bestFlight != null) {
            allFlights.add(0, bestFlight)
        }
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.getPackageParams().isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)

        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        resultsPresenter.resultsViewModel.flightResultsObservable.onNext(allFlights)

        if (!isOutboundResultsPresenter() && Db.getPackageSelectedOutboundFlight() != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.getPackageSelectedOutboundFlight())
        }
        val numTravelers = Db.getPackageParams().guests
        overviewPresenter.vm.selectedFlightLegSubject.subscribe { selectedFlight ->
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
        val destinationOrOrigin = if (isOutboundResultsPresenter()) Db.getPackageParams().destination else Db.getPackageParams().origin
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        toolbarViewModel.regionNames.onNext(destinationOrOrigin?.regionNames)
        toolbarViewModel.country.onNext(destinationOrOrigin?.hierarchyInfo?.country?.name)
        toolbarViewModel.airport.onNext(destinationOrOrigin?.hierarchyInfo?.airport?.airportCode)
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundResultsPresenter()) Db.getPackageParams().startDate else Db.getPackageParams().endDate as LocalDate)
        toolbarViewModel.lob.onNext(getLineOfBusiness())
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView()) {
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
            bundleSlidingWidget.bundlePriceFooter.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        }
        overviewPresenter.vm.obFeeDetailsUrlObservable.subscribe(paymentFeeInfoWebView.viewModel.webViewURLObservable)
        Db.getPackageParams().flightLegList = allFlights
        trackFlightResultsLoad()
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
            selectedFlightResults.onNext(Db.getPackageSelectedOutboundFlight())
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

    override fun isOutboundResultsPresenter(): Boolean = Db.getPackageParams()?.isOutboundSearch(isMidAPIEnabled(context)) ?: false

    override fun trackFlightOverviewLoad() {
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch(isMidAPIEnabled(context)) ?: false
        PackagesTracking().trackFlightRoundTripDetailsLoad(isOutboundSearch)
    }

    override fun trackFlightSortFilterLoad() {
        PackagesTracking().trackFlightSortFilterLoad()
    }

    override fun trackFlightResultsLoad() {
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch(isMidAPIEnabled(context)) ?: false
        PackagesTracking().trackFlightRoundTripLoad(isOutboundSearch, Db.getPackageParams(), if (isOutboundSearch) PackagesPageUsableData.FLIGHT_OUTBOUND.pageUsableData else PackagesPageUsableData.FLIGHT_INBOUND.pageUsableData)
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
            show(bundleSlidingWidget)
        }
        slidingBundleWidgetListener =  SlidingBundleWidgetListener(bundleSlidingWidget, this)
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)
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
