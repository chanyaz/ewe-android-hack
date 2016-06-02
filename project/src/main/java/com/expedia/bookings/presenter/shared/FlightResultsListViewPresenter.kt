package com.expedia.bookings.presenter.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterButtonWithCountWidget
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
    private val filterButton: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    lateinit private var flightListAdapter: FlightListAdapter

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    // outputs
    val showSortAndFilterViewSubject = PublishSubject.create<Unit>()

    var isShowingOutboundResults = false

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val selectedOutboundFlightViewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context)
        dockedOutboundFlightSelection.viewModel = selectedOutboundFlightViewModel
        outboundFlightSelectedSubject.subscribe { positionChildren() }
        setupFilterButton()
    }

    fun setAdapter(adapter: FlightListAdapter) {
        flightListAdapter = adapter
        recyclerView.adapter = adapter
    }

    fun setLoadingState() {
        filterButton.visibility = GONE
        flightListAdapter.setLoadingState()
        positionChildren()
    }

    var resultsViewModel: FlightResultsViewModel by notNullAndObservable { vm ->
        vm.flightResultsObservable.subscribe(listResultsObserver)
        vm.isOutboundResults.subscribe { this.isShowingOutboundResults = it }
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        flightListAdapter.setNewFlights(it)
        filterButton.visibility = VISIBLE
    }

    private fun setupFilterButton() {
        recyclerView.addOnScrollListener(filterButton.hideShowOnRecyclerViewScrollListener())
        filterButton.setButtonBackgroundColor(R.color.lob_packages_primary_color)
        filterButton.setTextAndFilterIconColor(R.color.white)
        filterButton.setOnClickListener { showSortAndFilterViewSubject.onNext(Unit) }
        filterButton.visibility = View.GONE
    }

    private fun positionChildren() {
        val isShowingDockedOutboundFlightWidget = !isShowingOutboundResults
        val onLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val location = IntArray(2)
                dockedOutboundFlightSelection.getLocationOnScreen(location)
                val top = location[1]
                val dockedOutboundFlightSelectionBottom = top + dockedOutboundFlightSelection.height - Ui.getStatusBarHeight(context)

                if (isShowingDockedOutboundFlightWidget) {
                    val newDropShadowLayoutParams = dockedOutboundFlightShadow.layoutParams as android.widget.FrameLayout.LayoutParams
                    newDropShadowLayoutParams.topMargin = dockedOutboundFlightSelectionBottom
                    dockedOutboundFlightShadow.layoutParams = newDropShadowLayoutParams
                }
                val newLayoutParams = recyclerView.layoutParams as android.widget.FrameLayout.LayoutParams
                newLayoutParams.topMargin = if (isShowingDockedOutboundFlightWidget) dockedOutboundFlightSelectionBottom else Ui.getToolbarSize(context)
                recyclerView.layoutParams = newLayoutParams

                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(onLayoutListener)
    }
}
