package com.expedia.vm.traveler

import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FrequentFlyerCard
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.utils.FlightV2Utils
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class FrequentFlyerAdapterViewModel(var traveler: Traveler) {

    val flightLegsObservable = PublishSubject.create<List<FlightLeg>>()
    val frequentFlyerPlans = PublishSubject.create<FlightCreateTripResponse.FrequentFlyerPlans>()
    val frequentFlyerCardsObservable = PublishSubject.create<List<FrequentFlyerCard>>()
    val updateTravelerObservable = PublishSubject.create<Traveler>()
    val viewHolderViewModels = ArrayList<FlightTravelerFrequentFlyerItemViewModel>()
    val showFrequentFlyerObservable = PublishSubject.create<Boolean>()

    init {
        updateTravelerObservable.subscribe { traveler ->
            this.traveler = traveler
        }

        ObservableOld.combineLatest(flightLegsObservable, frequentFlyerPlans, { legs, plans ->
            updateFrequentFlyerData(legs, plans)
        }).subscribe()

        updateTravelerObservable.withLatestFrom(flightLegsObservable, frequentFlyerPlans, { _, legs, plans ->
            updateFrequentFlyerData(legs, plans)
        }).subscribe()
    }

    private fun updateFrequentFlyerData(legs: List<FlightLeg>, plans: FlightCreateTripResponse.FrequentFlyerPlans) {
        if (legs != null && plans != null) {
            viewHolderViewModels.clear()
            val validAirlines = createFrequentFlyerCards(legs, plans)
            val hasValidAirlines = validAirlines.isNotEmpty()
            if (hasValidAirlines) {
                frequentFlyerCardsObservable.onNext(validAirlines)
            }
            showFrequentFlyerObservable.onNext(hasValidAirlines)
        }
    }

    private fun createFrequentFlyerCards(legs: List<FlightLeg>, plans: FlightCreateTripResponse.FrequentFlyerPlans): ArrayList<FrequentFlyerCard> {
        val validAirlines = ArrayList<FrequentFlyerCard>()
        updateFlightTravelerFrequentFlyerItemViewModel(legs, plans, validAirlines)
        return validAirlines
    }

    private fun updateFlightTravelerFrequentFlyerItemViewModel(legs: List<FlightLeg>, plans: FlightCreateTripResponse.FrequentFlyerPlans, validAirlines: ArrayList<FrequentFlyerCard>) {
        val airlines = FlightV2Utils.getAirlineNames(legs)
        airlines.forEach { airline ->
            val viewModel = FlightTravelerFrequentFlyerItemViewModel(traveler)
            setUpFrequentFlyerPlans(plans, viewModel)
            if (viewModel.allAirlineCodes.contains(airline.airlineCode)) {
                viewHolderViewModels.add(viewModel)
                validAirlines.add(airline)
                viewModel.bind(airline)
            }
        }
    }

    private fun setUpFrequentFlyerPlans (frequentFlyerPlans: FlightCreateTripResponse.FrequentFlyerPlans, viewModel: FlightTravelerFrequentFlyerItemViewModel) {
        frequentFlyerPlans.allFrequentFlyerPlans?.forEachIndexed { index, it ->
            val formattedAirlineCode = it.airlineCode.replace(" ", "")
            viewModel.allFrequentFlyerPlans.put(formattedAirlineCode, it)
            viewModel.allAirlineCodes.add(index, formattedAirlineCode)
        }

        frequentFlyerPlans.enrolledFrequentFlyerPlans?.forEach {
            val formattedAirlineCode = it.airlineCode.replace(" ", "")
            viewModel.enrolledPlans.put(formattedAirlineCode, it)
        }
    }
}
