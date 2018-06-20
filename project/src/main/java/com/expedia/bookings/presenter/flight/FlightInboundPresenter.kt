package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import javax.inject.Inject

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    init {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    override fun setupComplete() {
        super.setupComplete()
        flightOfferViewModel.confirmedLegSelection.filter { it == 0 }.map { Db.getFlightSearchParams().latestSelectedOfferInfo.selectedLegList[0] }
                .subscribe(resultsPresenter.outboundFlightSelectedSubject)
        detailsPresenter.vm.selectedFlightClickedSubject.subscribe {
            val params = Db.getFlightSearchParams()
            params.latestSelectedOfferInfo.selectedLegList[flightOfferViewModel.currentLeg] = it
            flightOfferViewModel.confirmedLegSelection.onNext(flightOfferViewModel.currentLeg)
        }
        detailsPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.inboundSelected)
        flightOfferViewModel.inboundResultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightByotSearch)) {
            flightOfferViewModel.confirmedLegSelection.filter { it == 0 }.subscribe {
                resultsPresenter.setLoadingState()
            }
        }
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return false
    }

    override fun trackFlightOverviewLoad(flight: FlightLeg) {
        FlightsV2Tracking.trackFlightOverview(false, true, flight)
    }

    override fun trackFlightScrollDepth(scrollDepth: Int) {
        FlightsV2Tracking.trackSRPScrollDepth(scrollDepth, false, true, flightOfferViewModel.totalInboundResults)
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightResultsLoad() {
        val trackingData = searchTrackingBuilder.build()
        FlightsV2Tracking.trackResultInBoundFlights(trackingData, Pair(Db.getFlightSearchParams().latestSelectedOfferInfo.selectedLegList[0].legRank, flightOfferViewModel.totalOutboundResults))
    }

    class FlightInboundMissingTransitionException(exceptionMessage: String) : RuntimeException(exceptionMessage)
    override fun missingTransitionException(exceptionMessage: String): RuntimeException {
        return FlightInboundMissingTransitionException(exceptionMessage)
    }
}
