package com.expedia.bookings.presenter.flight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.vm.flights.FlightOffersViewModel
import com.expedia.vm.flights.TripType
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import javax.inject.Inject


class FlightResultsPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    init {
        Ui.getApplication(context).flightComponent().inject(this)
        flightOfferViewModel = FlightOffersViewModel(context, flightServices)
        setupComplete()
    }

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
        flightOfferViewModel.resultsObservable.subscribe { it ->
            when (it) {
                is TripType.OneWay -> {

                }
                else -> {
                    val returnIntent = Intent()
                    if(detailsPresenter.vm.selectedFlightLegSubject.value != null){
                        val gson = GsonBuilder()
                                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                                .create()
                        returnIntent.putExtra("results", gson.toJson(it))

                        val activity = (context as AppCompatActivity)
                        activity.setResult(Activity.RESULT_OK, returnIntent)
                        activity.finish()
                    }
                }
            }

        }
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
        //val trackingData = searchTrackingBuilder.build()
       // FlightsV2Tracking.trackResultOutBoundFlights(trackingData, flightOfferViewModel.isSubPub)
    }

    class FlightOutboundMissingTransitionException(exceptionMessage: String) : RuntimeException(exceptionMessage)

    override fun missingTransitionException(exceptionMessage: String): RuntimeException {
        return FlightOutboundMissingTransitionException(exceptionMessage)
    }
}
