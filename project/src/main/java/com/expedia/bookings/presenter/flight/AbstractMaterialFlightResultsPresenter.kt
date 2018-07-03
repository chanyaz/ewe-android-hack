package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.isRichContentEnabled
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.bookings.flights.vm.FlightResultsViewModel
import com.expedia.bookings.flights.vm.FlightToolbarViewModel
import com.expedia.bookings.flights.vm.BaseFlightOffersViewModel
import com.expedia.bookings.flights.vm.FlightOverviewViewModel
import com.expedia.bookings.presenter.Presenter

abstract class AbstractMaterialFlightResultsPresenter(context: Context, attrs: AttributeSet?) : BaseFlightPresenter(context, attrs) {

    lateinit var flightOfferViewModel: BaseFlightOffersViewModel

    protected val defaultTransition = object : Presenter.DefaultTransition(FlightOutboundPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            Db.getFlightSearchParams().currentLeg = resultsPresenter.resultsViewModel.getLegNo()
        }
    }

    init {
        toolbarViewModel = FlightToolbarViewModel(context)
        toolbarViewModel.menuVisibilitySubject.subscribe { menuSearch.isVisible = it }
    }

    open fun setupComplete() {
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject, flightOfferViewModel.isRoundTripSearchSubject,
                isOutboundResultsPresenter(), flightOfferViewModel.flightCabinClassSubject, flightOfferViewModel.nonStopSearchFilterAppliedSubject,
                flightOfferViewModel.refundableFilterAppliedSearchSubject)

        flightOfferViewModel.searchResultsObservable
                .filter { it.first == resultsPresenter.resultsViewModel.getLegNo() }
                .map { it.second }
                .subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)

        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            val travelerParams = Db.getFlightSearchParams()
            if (travelerParams != null) {
                detailsPresenter.vm.numberOfTravelers.onNext(travelerParams.guests)
            }
            show(resultsPresenter, FLAG_CLEAR_BACKSTACK)
            flightListAdapter.initializeScrollDepthMap()
            resultsPresenter.trackScrollDepthSubscription = flightListAdapter.trackScrollDepthSubject.subscribe {
                trackFlightScrollDepth(it)
            }
            filter.viewModelBase.atleastOneFilterIsApplied.filter { it }.subscribe {
                resultsPresenter.trackScrollDepthSubscription?.dispose()
            }
        }
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        resultsPresenter.flightSelectedSubject.subscribe {
            if (isRichContentEnabled(context) && it.richContent == null) {
                val searchParam = Db.getFlightSearchParams()
                FlightsV2Tracking.trackRouteHappyNotDisplayed(resultsPresenter.isShowingOutboundResults, searchParam.isRoundTrip())
                resultsPresenter.resultsViewModel.isRoutehappyOmnitureTrigerred = true
            }
        }
    }

    override fun back(): Boolean {
        flightOfferViewModel.cancelSearchObservable.onNext(Unit)
        return super.back()
    }

    fun displayPaymentFeeHeaderInfo(mayChargePaymentFees: Boolean) {
        var paymentFeeText = ""
        if (mayChargePaymentFees) {
            paymentFeeText = context.resources.getString(R.string.airline_additional_fee_notice)
        }
        detailsPresenter.vm.airlinePaymentFeesHeaderSubject.onNext(paymentFeeText)
        resultsPresenter.resultsViewModel.airlineChargesFeesSubject.onNext(mayChargePaymentFees)
    }

    override fun makeFlightOverviewModel(): AbstractFlightOverviewViewModel {
        return FlightOverviewViewModel(context)
    }

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.flights_toolbar_menu)
    }

    override fun trackShowBaggageFee() = FlightsV2Tracking.trackFlightBaggageFeeClick()

    override fun viewBundleSetVisibility(forward: Boolean) {
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun getResultsViewModel(context: Context): FlightResultsViewModel {
        return FlightResultsViewModel(context)
    }

    abstract fun trackFlightScrollDepth(scrollDepth: Int)
}
