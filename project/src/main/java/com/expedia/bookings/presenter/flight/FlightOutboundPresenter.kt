package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isFlexEnabled
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.vm.FlightWebCheckoutViewViewModel
import com.expedia.vm.flights.BaseFlightOffersViewModel
import com.expedia.vm.flights.FlightOffersViewModel
import com.expedia.vm.flights.FlightOffersViewModelByot
import javax.inject.Inject

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        flightOfferViewModel =  FlightOffersViewModel(context, flightServices)
        setupComplete()
    }

//    val flightOfferViewModel: BaseFlightOffersViewModel by lazy {
//        val viewModel: BaseFlightOffersViewModel
//
//        viewModel = FlightOffersViewModel(context, flightServices)
//        viewModel
//    }

    override fun back(): Boolean {
        flightOfferViewModel.cancelOutboundSearchObservable.onNext(Unit)
        if (isFlightGreedySearchEnabled(context)) {
            flightOfferViewModel.isGreedyCallAborted = true
            flightOfferViewModel.cancelGreedyCalls()
        }
        return super.back()
    }

    override fun setupComplete() {
        flightOfferViewModel.searchParamsObservable.onNext(Db.getFlightSearchParams())

        super.setupComplete()
        flightOfferViewModel.searchParamsObservable.subscribe {
            resultsPresenter.setLoadingState()
        }
        flightOfferViewModel.resultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultObservable)
        detailsPresenter.vm.selectedFlightClickedSubject.subscribe(flightOfferViewModel.confirmedOutboundFlightSelection)
        detailsPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.outboundSelected)
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad(flight: FlightLeg) {
        val isRoundTrip = flightOfferViewModel.isRoundTripSearchSubject.value
        FlightsV2Tracking.trackFlightOverview(true, isRoundTrip, flight)
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightScrollDepth(scrollDepth: Int) {
        FlightsV2Tracking.trackSRPScrollDepth(scrollDepth, true, flightOfferViewModel.isRoundTripSearchSubject.value, flightOfferViewModel.totalOutboundResults)
    }

    override fun trackFlightResultsLoad() {
        val trackingData = searchTrackingBuilder.build()
        FlightsV2Tracking.trackResultOutBoundFlights(trackingData, flightOfferViewModel.isSubPub)
    }

    class FlightOutboundMissingTransitionException(exceptionMessage: String) : RuntimeException(exceptionMessage)
    override fun missingTransitionException(exceptionMessage: String): RuntimeException {
        return FlightOutboundMissingTransitionException(exceptionMessage)
    }
}
