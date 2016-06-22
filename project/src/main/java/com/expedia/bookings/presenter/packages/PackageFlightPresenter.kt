package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.View
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.PackageFlightListAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.bookings.widget.SlidingBundleWidgetListener
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility

class PackageFlightPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    val bundleSlidingWidget: SlidingBundleWidget by bindView(R.id.sliding_bundle_widget)

    init {
        toolbarViewModel.menuVisibilitySubject.subscribe { showMenu ->
            menuFilter.isVisible = if (showMenu) true else false
        }
    }

    private val flightOverviewSelected = endlessObserver<FlightLeg> { flight ->
        val params = Db.getPackageParams()
        if (flight.outbound) {
            Db.setPackageSelectedOutboundFlight(flight)
            params.currentFlights[0] = flight.legId
        } else {
            Db.setPackageSelectedInboundFlight(flight)
            params.currentFlights[1] = flight.legId
        }
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_FLIGHT)

        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }


    private fun setupMenuFilter() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        val filterCountText = toolbarFilterItemActionView.findViewById(R.id.filter_count_text) as TextView
        val filterPlaceholderImageView = toolbarFilterItemActionView.findViewById(R.id.filter_placeholder_icon) as ImageView
        val filterButtonText = toolbarFilterItemActionView.findViewById(R.id.filter_text) as TextView
        val filterBtn = toolbarFilterItemActionView.findViewById(R.id.filter_btn) as LinearLayout
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

        menuFilter.actionView = toolbarFilterItemActionView
        filter.viewModel.filterCountObservable.map { it.toString() }.subscribeText(filterCountText)
        filter.viewModel.filterCountObservable.map { it > 0 }.subscribeVisibility(filterCountText)
        filter.viewModel.filterCountObservable.map { it > 0 }.subscribeInverseVisibility(filterPlaceholderImageView)
    }

    override fun addResultOverViewTransition() {
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.getPackageSelectedOutboundFlight())
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.getPackageSelectedInboundFlight())
        } else {
            super.addResultOverViewTransition()
        }
    }

    private fun addBackFlowTransition() {
        addDefaultTransition(backFlowDefaultTransition)
        addTransition(backFlowOverviewTransition)
        show(resultsPresenter)
        show(overviewPresenter)
    }

    init {
        View.inflate(getContext(), R.layout.package_flight_presenter, this)
        resultsPresenter.showFilterButton = false
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            val params = Db.getPackageParams()
            params.selectedLegId = null
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE))
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE))
        }

        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_FLIGHT)
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch() ?: false
        val bestPlusAllFlights = Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch && it.packageOfferModel != null }

        // move bestFlight to the first place of the list
        val bestFlight = bestPlusAllFlights.find { it.isBestFlight }
        val allFlights = bestPlusAllFlights.filterNot { it.isBestFlight }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount }.toMutableList()

        allFlights.add(0, bestFlight)
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.getPackageParams().isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)
        resultsPresenter.resultsViewModel.flightResultsObservable.onNext(allFlights)
        if (!isOutboundResultsPresenter() && Db.getPackageSelectedOutboundFlight() != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.getPackageSelectedOutboundFlight())
        }
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOverviewSelected)
        var cityBound: String = if (isOutboundResultsPresenter()) Db.getPackageParams().destination.regionNames.shortName else Db.getPackageParams().origin.regionNames.shortName
        val numTravelers = Db.getPackageParams().guests
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        toolbarViewModel.city.onNext(cityBound)
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundResultsPresenter()) Db.getPackageParams().checkIn else Db.getPackageParams().checkOut)
        trackFlightResultsLoad()
    }

    override fun isOutboundResultsPresenter(): Boolean = Db.getPackageParams()?.isOutboundSearch() ?: false

    override fun trackFlightOverviewLoad() {
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch() ?: false
        PackagesTracking().trackFlightRoundTripDetailsLoad(isOutboundSearch)
    }

    override fun trackFlightSortFilterLoad() {
        PackagesTracking().trackFlightSortFilterLoad()
    }

    override fun trackFlightResultsLoad() {
        PackagesTracking().trackFlightRoundTripLoad(Db.getPackageParams()?.isOutboundSearch() ?: false)
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
            back()
        }
        bundleSlidingWidget.bundleOverViewWidget.inboundFlightWidget.rowContainer.setOnClickListener {
            back()
        }
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
            show(bundleSlidingWidget)
        }

        val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this)
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

    private val resultsToOverview = bundleSlidingWidget.addBundleTransitionFrom(FlightResultsListViewPresenter::class.java.name)

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.package_flights_menu)
    }

    override fun trackShowBaggageFee() = PackagesTracking().trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() {
        // do nothing. Not applicable to Packages LOB
    }

    override fun shouldShowBundlePrice(): Boolean {
        return true
    }
}
