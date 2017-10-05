package com.expedia.bookings.presenter.shared

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.FeatureToggleUtil
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
import com.expedia.vm.flights.DockedOutboundFlightV2ViewModel
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import rx.Subscription
import rx.subjects.PublishSubject

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    private val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    private val airlineChargesFeesTextView: TextView by bindView(R.id.airline_charges_fees_header)
    val filterButton: FlightFilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    lateinit private var flightListAdapter: AbstractFlightListAdapter
    var trackScrollDepthSubscription: Subscription? = null

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

    fun bind(lob: LineOfBusiness) {
        val dockedOutboundFlightSelectionStub = findViewById<ViewStub>(R.id.widget_docked_outbound_flight_stub)

        val shouldShowDeltaPricing = lob == LineOfBusiness.FLIGHTS_V2 && FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
                AbacusUtils.EBAndroidAppFlightsDeltaPricing, R.string.preference_flight_delta_pricing)
        if (shouldShowDeltaPricing) {
            dockedOutboundFlightSelectionStub.layoutResource = R.layout.docked_outbound_flight_selection_v2
            val dockedOutboundFlightSelection = dockedOutboundFlightSelectionStub.inflate() as DockedOutboundFlightWidgetV2
            dockedOutboundFlightSelection.viewModel = DockedOutboundFlightV2ViewModel(outboundFlightSelectedSubject, context)
            this.dockedOutboundFlightSelection = dockedOutboundFlightSelection
        } else {
            dockedOutboundFlightSelectionStub.layoutResource = R.layout.docked_outbound_flight_selection
            val dockedOutboundFlightSelection = dockedOutboundFlightSelectionStub.inflate() as DockedOutboundFlightSelectionView
            dockedOutboundFlightSelection.viewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context)
            this.dockedOutboundFlightSelection = dockedOutboundFlightSelection
        }

        outboundFlightSelectedSubject.subscribe { positionChildren() }
        setupFilterButton()
    }

    override fun back(): Boolean {
        trackScrollDepthSubscription?.unsubscribe()
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
        vm.flightResultsObservable.subscribe(listResultsObserver)
        vm.isOutboundResults.subscribe { isShowingOutboundResults = it }
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)

        vm.airlineChargesFeesSubject.subscribe { showAirlineChargesFees ->
            setPaymentLegalMessage(showAirlineChargesFees)
        }
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        positionChildren()
        flightListAdapter.setNewFlights(it)
        filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
    }

    private fun setupFilterButton() {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.hotel_select_room_ripple_drawable, outValue, true)
        filterButton.setBackground(outValue.resourceId)
        filterButton.setTextAndFilterIconColor(R.color.white)
        filterButton.setOnClickListener {
            showSortAndFilterViewSubject.onNext(Unit)
        }
    }

    private fun positionChildren() {
        val isShowingDockedOutboundFlightWidget = !isShowingOutboundResults

        fun resetChildrenTops() {
            val toolbarSize = getToolbarSize()
            airlineChargesFeesTextView.top = 0
            dockedOutboundFlightShadow.top = 0
            recyclerView.top = toolbarSize.toInt()
        }

        if (isShowingDockedOutboundFlightWidget) {
            dockedOutboundFlightSelection.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    resetChildrenTops()
                    if (dockedOutboundFlightSelection.height != 0) {
                        dockedOutboundFlightSelection.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        airlineChargesFeesTextView.translationY = getToolbarSize()
                        dockedOutboundFlightSelection.translationY = getToolbarSize() + airlineChargesFeesTextView.height.toFloat()
                        dockedOutboundFlightShadow.translationY = getToolbarSize() + airlineChargesFeesTextView.height.toFloat() + dockedOutboundFlightSelection.height.toFloat()
                        recyclerView.translationY = airlineChargesFeesTextView.height.toFloat() + dockedOutboundFlightSelection.height.toFloat()
                        val layoutParams: LayoutParams = recyclerView.layoutParams as LayoutParams
                        layoutParams.bottomMargin = dockedOutboundFlightSelection.height
                    }
                }
            })
        } else {
            airlineChargesFeesTextView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    resetChildrenTops()
                    if (airlineChargesFeesTextView.height != 0) {
                        airlineChargesFeesTextView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        airlineChargesFeesTextView.translationY = getToolbarSize()
                        recyclerView.translationY = airlineChargesFeesTextView.height.toFloat()
                    }
                }
            })
        }
    }

    private fun getToolbarSize(): Float = Ui.getToolbarSize(context).toFloat()
    fun getAirlinePaymentFeesTextView() = airlineChargesFeesTextView
}
