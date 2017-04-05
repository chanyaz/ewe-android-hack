package com.expedia.bookings.presenter.shared

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightFilterButtonWithCountWidget
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import rx.subjects.PublishSubject

class FlightResultsListViewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    private val dockedOutboundFlightSelection: DockedOutboundFlightSelectionView by bindView(R.id.docked_outbound_flight_selection)
    private val dockedOutboundFlightShadow: View by bindView(R.id.docked_outbound_flight_widget_dropshadow)
    private val airlineChargesFeesTextView: TextView by bindView(R.id.airline_charges_fees_header)
    val filterButton: FlightFilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    lateinit private var flightListAdapter: AbstractFlightListAdapter

    // input
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    // outputs
    val showSortAndFilterViewSubject = PublishSubject.create<Unit>()

    var isShowingOutboundResults = false
    var showFilterButton = true

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message)) {
            if (PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
                airlineChargesFeesTextView.text = context.getString(R.string.airline_additional_fee_notice)
            }
        } else {
            if (PointOfSale.getPointOfSale().airlineMayChargePaymentMethodFee()) {
                airlineChargesFeesTextView.text = context.getString(R.string.airline_may_charge_notice)
            } else {
                airlineChargesFeesTextView.text = context.getString(R.string.airline_charge_notice)
            }
        }
        val selectedOutboundFlightViewModel = SelectedOutboundFlightViewModel(outboundFlightSelectedSubject, context)
        dockedOutboundFlightSelection.viewModel = selectedOutboundFlightViewModel
        outboundFlightSelectedSubject.subscribe { positionChildren() }
        setupFilterButton()
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
        vm.isOutboundResults.subscribe { this.isShowingOutboundResults = it }
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightSelection)
        vm.isOutboundResults.subscribeInverseVisibility(dockedOutboundFlightShadow)
        vm.airlineChargesFeesSubject.subscribeVisibility(airlineChargesFeesTextView)
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        positionChildren()
        flightListAdapter.setNewFlights(it)
        filterButton.visibility = if (showFilterButton) View.VISIBLE else View.GONE
    }

    private fun setupFilterButton() {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightStaticSortFilter)) {
            filterButton.visibility = View.VISIBLE
        }
        else {
            recyclerView.addOnScrollListener(filterButton.hideShowOnRecyclerViewScrollListener())
            filterButton.visibility = View.GONE
        }
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
                        dockedOutboundFlightSelection.translationY = airlineChargesFeesTextView.height.toFloat()
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
}
