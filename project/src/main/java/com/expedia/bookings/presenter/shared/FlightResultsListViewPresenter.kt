package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.animation.ProgressBarAnimation
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.flights.vm.SelectedOutboundFlightViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isHighlightSortFilterOnPackagesEnabled
import com.expedia.bookings.utils.isRichContentEnabled
import com.expedia.bookings.widget.FlightFilterButtonWithCountWidget
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.FlightLoadingWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.bookings.widget.flights.DockedOutboundFlightWidgetV2
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.bookings.widget.shared.SortFilterFloatingActionPill
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseResultsViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    private val airlineChargesFeesTextView: TextView by bindView(R.id.airline_charges_fees_header)
    val filterButton: FlightFilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    val floatingPill: SortFilterFloatingActionPill by bindView(R.id.flight_results_floating_pill)
    private lateinit var flightLoader: ViewStub
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var flightLoadingWidget: FlightLoadingWidget
    lateinit var flightProgressBar: ProgressBar

    private lateinit var flightListAdapter: AbstractFlightListAdapter
    var trackScrollDepthSubscription: Disposable? = null
    var anim: ProgressBarAnimation? = null

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()
    val showPaymentLegalMessageSubject = PublishSubject.create<Unit>()

    // outputs
    val showSortAndFilterViewSubject = PublishSubject.create<Unit>()

    var isShowingOutboundResults = false
    var showFilterButton = true
    var showFilterPill = false

    private val FLIGHT_PROGRESS_BAR_MAX = 600

    lateinit var dockedOutboundFlightSelection: View

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    private fun bind(doNotOverrideFilterButton: Boolean, lob: LineOfBusiness) {
        showFilterPill = lob == LineOfBusiness.PACKAGES && isHighlightSortFilterOnPackagesEnabled(context)
        val dockedOutboundFlightSelectionStub = findViewById<ViewStub>(R.id.widget_docked_outbound_flight_stub)
        val selectedOutboundFlightViewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context, lob)
        if (lob == LineOfBusiness.FLIGHTS_V2) {
            dockedOutboundFlightSelectionStub.layoutResource = R.layout.docked_outbound_flight_selection_v2
            val dockedOutboundFlightWidgetV2 = dockedOutboundFlightSelectionStub.inflate() as DockedOutboundFlightWidgetV2
            dockedOutboundFlightWidgetV2.viewModel = selectedOutboundFlightViewModel
            this.dockedOutboundFlightSelection = dockedOutboundFlightWidgetV2
        } else {
            dockedOutboundFlightSelectionStub.layoutResource = R.layout.docked_outbound_flight_selection
            val dockedOutboundFlightWidget = dockedOutboundFlightSelectionStub.inflate() as DockedOutboundFlightSelectionView
            dockedOutboundFlightWidget.viewModel = selectedOutboundFlightViewModel
            this.dockedOutboundFlightSelection = dockedOutboundFlightWidget
        }
        setupFilterButton(doNotOverrideFilterButton)
        if (showFilterPill) {
            setupFilterPill()
        }
    }

    override fun back(): Boolean {
        trackScrollDepthSubscription?.dispose()
        if (isRichContentEnabled(context)) {
            if (isShowingOutboundResults) {
                resultsViewModel.abortRichContentOutboundObservable.onNext(Unit)
            } else {
                resultsViewModel.abortRichContentInboundObservable.onNext(Unit)
            }
        }
        return super.back()
    }

    private fun setPaymentLegalMessage(showLegalPaymentMessage: Boolean) {
        if (showLegalPaymentMessage) {
            airlineChargesFeesTextView.text = context.getString(R.string.airline_additional_fee_notice)
            airlineChargesFeesTextView.visibility = View.VISIBLE
        } else {
            airlineChargesFeesTextView.visibility = View.GONE
        }
    }

    fun setAdapter(adapter: AbstractFlightListAdapter) {
        flightListAdapter = adapter
        recyclerView.adapter = adapter
    }

    fun setLoadingState() {
        filterButton.visibility = GONE
        floatingPill.visibility = GONE
        airlineChargesFeesTextView.visibility = View.GONE
        if (isShowingOutboundResults && resultsViewModel.showLoadingStateV1 && Db.getFlightSearchParams() != null) {
            showLoadingStateV1()
        }
        flightListAdapter.setLoadingState()
    }

    private fun showLoadingStateV1() {
        flightProgressBar = findViewById(R.id.flight_loader_progressBar)
        flightProgressBar.visibility = View.VISIBLE
        flightProgressBar.alpha = 1f
        flightProgressBar.translationY = 0f
        flightLoadingWidget.setupLoadingState()
        flightProgressBar.max = FLIGHT_PROGRESS_BAR_MAX
        progressBarAnimation(12000, 0f, 500f, false, false)
    }

    var resultsViewModel: BaseResultsViewModel by notNullAndObservable { vm ->
        bind(vm.doNotOverrideFilterButton, vm.getLineOfBusiness())
        vm.flightResultsObservable.subscribe(listResultsObserver)
        vm.isOutboundResults.subscribe { isShowingOutboundResults = it }
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)
        if (vm.showLoadingStateV1) {
            flightLoader = findViewById(R.id.flight_loading_screen)
            flightLoader.visibility = View.VISIBLE
            flightLoadingWidget = findViewById(R.id.flight_loading_view)
            showPaymentLegalMessageSubject.withLatestFrom(vm.airlineChargesFeesSubject, { _, showAirlineChargesFees -> showAirlineChargesFees })
                    .subscribe {
                        setPaymentLegalMessage(it)
                    }
            vm.updateFlightsStream.subscribe {
                if (isShowingOutboundResults) {
                    completeProgressBarAnimation()
                }
            }
            vm.isOutboundResults.subscribeVisibility(flightLoadingWidget)
        } else {
            vm.airlineChargesFeesSubject.subscribe { showAirlineChargesFees ->
                setPaymentLegalMessage(showAirlineChargesFees)
            }
        }
        vm.updateFlightsStream.subscribe { flightListAdapter.notifyItemRangeChanged(0, flightListAdapter.itemCount) }
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        flightListAdapter.setNewFlights(it)
        if (resultsViewModel.showLoadingStateV1) {
            if (isShowingOutboundResults) {
                flightProgressBar.clearAnimation()
                flightLoadingWidget.setResultReceived()
                if (resultsViewModel.showRichContent) {
                    progressBarAnimation(2000, flightProgressBar.progress.toFloat(), 580f, true, false)
                } else {
                    progressBarAnimation(1000, flightProgressBar.progress.toFloat(), FLIGHT_PROGRESS_BAR_MAX.toFloat(), true, true)
                }
            } else {
                showPaymentLegalMessageSubject.onNext(Unit)
                flightLoadingWidget.visibility = View.GONE
                setFilterButtonVisibility()
            }
        } else {
            setFilterButtonVisibility()
        }
    }

    private fun setFilterButtonVisibility() {
        filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
        floatingPill.visibility = if (showFilterPill) View.VISIBLE else View.GONE
    }

    private fun completeProgressBarAnimation() {
        progressBarAnimation(300, flightProgressBar.progress.toFloat(), FLIGHT_PROGRESS_BAR_MAX.toFloat(), true, true)
    }

    private fun progressBarAnimation(duration: Long, fromProgress: Float, toProgress: Float, resultsReceived: Boolean, hideProgressBar: Boolean) {
        anim?.cancel()
        anim = ProgressBarAnimation(flightProgressBar, fromProgress, toProgress)
        anim?.duration = duration
        flightProgressBar.startAnimation(anim)
        if (resultsReceived) {
            anim?.setAnimationListener(object : AnimationListenerAdapter() {
                override fun onAnimationEnd(animation: Animation?) {
                    super.onAnimationEnd(animation)
                    if (hideProgressBar) {
                        flightProgressBar.animate().translationYBy(-30f).alpha(0f).setDuration(300).withEndAction {
                            flightProgressBar.visibility = View.GONE
                            showPaymentLegalMessageSubject.onNext(Unit)
                        }
                    }
                    filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
                    floatingPill.visibility = if (showFilterPill) View.VISIBLE else View.GONE
                }
            })
        }
    }

    private fun setupFilterButton(doNotOverrideFilterButton: Boolean) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.hotel_select_room_ripple_drawable, outValue, true)
        if (!doNotOverrideFilterButton) {
            filterButton.setBackground(outValue.resourceId)
            filterButton.setTextAndFilterIconColor(R.color.white)
        }
        filterButton.setOnClickListener {
            showSortAndFilterViewSubject.onNext(Unit)
        }
    }

    private fun setupFilterPill() {
        floatingPill.visibility = View.VISIBLE
        floatingPill.setFlightsCompatibleMode()
        floatingPill.setOnClickListener {
            showSortAndFilterViewSubject.onNext(Unit)
        }
    }

    fun getAirlinePaymentFeesTextView() = airlineChargesFeesTextView
}
