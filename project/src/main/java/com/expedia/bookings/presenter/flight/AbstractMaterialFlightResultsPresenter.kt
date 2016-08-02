package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.flights.FlightOffersViewModel
import com.expedia.vm.flights.FlightOverviewViewModel

abstract class AbstractMaterialFlightResultsPresenter(context: Context, attrs: AttributeSet?) : BaseFlightPresenter(context, attrs) {

    lateinit var flightOfferViewModel: FlightOffersViewModel

    init {
        toolbarViewModel.menuVisibilitySubject.subscribe { menuSearch.isVisible = it }
    }

    open fun setupComplete() {
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            val numberOfTravelers = Db.getFlightSearchParams()?.guests
            if (numberOfTravelers != null) {
                overviewPresenter.vm.numberOfTravelers.onNext(numberOfTravelers)
            }
            show(resultsPresenter)
        }
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject, flightOfferViewModel.isRoundTripSearchSubject)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        flightOfferViewModel.obFeeDetailsUrlObservable.subscribe(paymentFeeInfoWebView.viewModel.webViewURLObservable)
        flightOfferViewModel.offerSelectedChargesObFeesSubject.subscribeTextAndVisibility(overviewPresenter.paymentFeesMayApplyTextView)
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
