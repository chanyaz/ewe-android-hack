package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.flights.FlightOverviewViewModel

abstract class AbstractMaterialFlightResultsPresenter(context: Context, attrs: AttributeSet?) : BaseFlightPresenter(context, attrs) {

    init {
        toolbarViewModel.menuVisibilitySubject.subscribe { menuSearch.isVisible = it }
        
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            val numberOfTravelers = Db.getFlightSearchParams()?.guests
            if (numberOfTravelers != null) {
                overviewPresenter.vm.numberOfTravelers.onNext(numberOfTravelers)
            }
            show(resultsPresenter)
        }
    }

    var flightSearchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject, vm)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        vm.confirmedOutboundFlightSelection.subscribe(resultsPresenter.outboundFlightSelectedSubject)
        vm.offerSelectedChargesObFeesSubject.subscribeTextAndVisibility(overviewPresenter.paymentFeesMayApplyTextView)
        vm.obFeeDetailsUrlObservable.subscribe(paymentFeeInfoWebView.viewModel.webViewURLObservable)
    }

    override fun makeFlightOverviewModel(): AbstractFlightOverviewViewModel {
        return FlightOverviewViewModel(context)
    }

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.flights_toolbar_menu)
    }

    override fun trackShowBaggageFee() = FlightsV2Tracking.trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() = FlightsV2Tracking.trackPaymentFeesClick()

    override fun viewBundleSetVisibility(forward: Boolean) {
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }
}
