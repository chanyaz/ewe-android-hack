package com.expedia.bookings.presenter.shared

import android.content.Context
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
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightFilterButtonWithCountWidget
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.bookings.widget.flights.DockedOutboundFlightWidgetV2
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import com.expedia.bookings.widget.FlightLoadingWidget

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    private val airlineChargesFeesTextView: TextView by bindView(R.id.airline_charges_fees_header)
    val filterButton: FlightFilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    private lateinit var flightLoader: ViewStub
    private lateinit var flightLoadingWidget: FlightLoadingWidget
    private lateinit var flightProgressBar: ProgressBar
    private lateinit var flightListAdapter: AbstractFlightListAdapter
    var trackScrollDepthSubscription: Disposable? = null

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    // outputs
    val showSortAndFilterViewSubject = PublishSubject.create<Unit>()

    var isShowingOutboundResults = false
    var showFilterButton = true

    private val FLIGHT_PROGRESS_BAR_MAX = 600

    lateinit var dockedOutboundFlightSelection: View

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    private fun bind(shouldShowDeltaPricing: Boolean, doNotOverrideFilterButton: Boolean) {
        val dockedOutboundFlightSelectionStub = findViewById<ViewStub>(R.id.widget_docked_outbound_flight_stub)
        val selectedOutboundFlightViewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context, shouldShowDeltaPricing)
        if (shouldShowDeltaPricing) {
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
    }

    override fun back(): Boolean {
        trackScrollDepthSubscription?.dispose()
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
        if (isShowingOutboundResults && resultsViewModel.showLoadingStateV1 && Db.getFlightSearchParams() != null) {
            showLoadingStateV1()
        }
        flightListAdapter.setLoadingState()
    }

    private fun showLoadingStateV1() {
        flightProgressBar = findViewById(R.id.flight_loader_progressBar)
        flightLoadingWidget.setupLoadingState()
        flightProgressBar.visibility = View.VISIBLE
        flightProgressBar.max = FLIGHT_PROGRESS_BAR_MAX
        progressBarAnimation(12000, 0f, 500f, false)
    }

    var resultsViewModel: FlightResultsViewModel by notNullAndObservable { vm ->
        bind(vm.shouldShowDeltaPricing, vm.doNotOverrideFilterButton)
        vm.flightResultsObservable.subscribe(listResultsObserver)
        vm.isOutboundResults.subscribe { isShowingOutboundResults = it }
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)
        vm.airlineChargesFeesSubject.subscribe { showAirlineChargesFees ->
            setPaymentLegalMessage(showAirlineChargesFees)
        }
        if (vm.showLoadingStateV1) {
            flightLoader = findViewById(R.id.flight_loading_screen)
            flightLoader.visibility = View.VISIBLE
            flightLoadingWidget = findViewById(R.id.flight_loading_view)
        }
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        flightListAdapter.setNewFlights(it)
        if (resultsViewModel.showLoadingStateV1) {
            if (isShowingOutboundResults) {
                flightProgressBar.clearAnimation()
                flightLoadingWidget.setResultReceived()
                progressBarAnimation(1000, flightProgressBar.progress.toFloat(), FLIGHT_PROGRESS_BAR_MAX.toFloat(), true)
            } else {
                flightLoadingWidget.visibility = View.GONE
            }
        }
        filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
    }

    private fun progressBarAnimation(duration: Long, fromProgress: Float, toProgress: Float, resultsReceived: Boolean) {
        val anim = ProgressBarAnimation(flightProgressBar, fromProgress, toProgress)
        anim.duration = duration
        flightProgressBar.startAnimation(anim)
        if (resultsReceived) {
            anim.setAnimationListener(object : AnimationListenerAdapter() {
                override fun onAnimationEnd(animation: Animation?) {
                    super.onAnimationEnd(animation)
                    flightProgressBar.visibility = View.GONE
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

    fun getAirlinePaymentFeesTextView() = airlineChargesFeesTextView
}
