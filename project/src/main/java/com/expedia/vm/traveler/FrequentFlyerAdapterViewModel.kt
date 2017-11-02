package com.expedia.vm.traveler

import com.expedia.bookings.ObservableOld
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FrequentFlyerCard
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
            viewHolderViewModels.forEach { viewModel ->
                viewModel.updateTraveler(traveler)
            }
        }

        ObservableOld.combineLatest(flightLegsObservable, frequentFlyerPlans, { legs, plans ->
            if (legs != null && plans != null) {
                viewHolderViewModels.clear()
                val validAirlines = ArrayList<FrequentFlyerCard>()
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
                val hasValidAirlines = validAirlines.isNotEmpty()
                if (hasValidAirlines) {
                    frequentFlyerCardsObservable.onNext(validAirlines)
                }
                showFrequentFlyerObservable.onNext(hasValidAirlines)
            }
        }).subscribe()
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