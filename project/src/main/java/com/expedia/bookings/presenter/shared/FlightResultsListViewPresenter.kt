package com.expedia.bookings.presenter.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import rx.subjects.PublishSubject

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    private val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    private val dockedOutboundFlightSelection: DockedOutboundFlightSelectionView by bindView(R.id.docked_outbound_flight_selection)
    private val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    lateinit private var flightListAdapter: FlightListAdapter

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val selectedOutboundFlightViewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context)
        dockedOutboundFlightSelection.viewModel = selectedOutboundFlightViewModel
    }

    fun setAdapter(adapter: FlightListAdapter) {
        flightListAdapter = adapter
        recyclerView.adapter = adapter
    }

    fun setLoadingState() {
        flightListAdapter.setLoadingState()
    }

    var resultsViewModel: FlightResultsViewModel by notNullAndObservable { vm ->
        vm.flightResultsObservable.subscribe(listResultsObserver)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        flightListAdapter.setNewFlights(it)
    }

}
