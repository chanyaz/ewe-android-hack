package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.flights.BaseFlightOffersViewModel
import com.expedia.vm.flights.FlightOverviewViewModel

abstract class AbstractMaterialFlightResultsPresenter(context: Context, attrs: AttributeSet?) : BaseFlightPresenter(context, attrs) {

    lateinit var flightOfferViewModel: BaseFlightOffersViewModel

    init {
        toolbarViewModel.menuVisibilitySubject.subscribe { menuSearch.isVisible = it }
    }

    open fun setupComplete() {
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            val travelerParams = Db.getFlightSearchParams()
            if (travelerParams != null) {
                overviewPresenter.vm.numberOfTravelers.onNext(travelerParams.guests)
            }
            resultsPresenter.lineOfBusinessSubject.onNext(getLineOfBusiness())
            show(resultsPresenter, FLAG_CLEAR_BACKSTACK)
        }
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject, flightOfferViewModel.isRoundTripSearchSubject,
                isOutboundResultsPresenter(), flightOfferViewModel.flightCabinClassSubject, flightOfferViewModel.nonStopSearchFilterAppliedSubject,
                flightOfferViewModel.refundableFilterAppliedSearchSubject)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        overviewPresenter.showPaymentFeesObservable.subscribe {
            paymentFeeInfoWebView.viewModel.webViewURLObservable.onNext(flightOfferViewModel.obFeeDetailsUrlObservable.value)
        }
        flightOfferViewModel.offerSelectedChargesObFeesSubject.subscribeTextAndVisibility(overviewPresenter.paymentFeesMayApplyTextView)
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppSimplifyFlightShopping)) {
            resultsPresenter.flightSelectedSubject.subscribe {
                overviewPresenter.vm.selectedFlightLegSubject.onNext(it)
                overviewPresenter.vm.selectFlightClickObserver.onNext(Unit)
            }
        }
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
