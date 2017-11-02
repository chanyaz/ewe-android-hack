package com.expedia.bookings.presenter.shared

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightFilterButtonWithCountWidget
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.bookings.widget.flights.DockedOutboundFlightWidgetV2
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    private val airlineChargesFeesTextView: TextView by bindView(R.id.airline_charges_fees_header)
    val filterButton: FlightFilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    lateinit private var flightListAdapter: AbstractFlightListAdapter
    var trackScrollDepthSubscription: Disposable? = null

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    // outputs
    val showSortAndFilterViewSubject = PublishSubject.create<Unit>()

    var isShowingOutboundResults = false
    var showFilterButton = true

    lateinit var dockedOutboundFlightSelection: View

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    private fun bind(shouldShowDeltaPricing: Boolean, doNotOverrideFilterButton: Boolean) {
        val dockedOutboundFlightSelectionStub = findViewById<ViewStub>(R.id.widget_docked_outbound_flight_stub)
        val selectedOutboundFlightViewModel =  SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context, shouldShowDeltaPricing)
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
        flightListAdapter.setLoadingState()
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
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        flightListAdapter.setNewFlights(it)
        filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
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
