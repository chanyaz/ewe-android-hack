package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.Observable
import rx.subjects.PublishSubject
import java.util.ArrayList


class FrequentFlyerAdapterViewModel(var traveler: Traveler) {

    val flightLegsObservable = PublishSubject.create<List<FlightLeg>>()
    val frequentFlyerPlans = PublishSubject.create<FlightCreateTripResponse.FrequentFlyerPlans>()
    val frequentFlyerCardsObservable = PublishSubject.create<List<FrequentFlyerCard>>()
    val updateTravelerObservable = PublishSubject.create<Traveler>()
    val viewHolderViewModels = ArrayList<FlightTravelerFrequentFlyerItemViewModel>()

    init {
        flightLegsObservable.subscribe { legs ->
            frequentFlyerCardsObservable.onNext(FlightV2Utils.getAirlineNames(legs))
        }

        updateTravelerObservable.subscribe { traveler ->
            this.traveler = traveler
            viewHolderViewModels.forEach { viewModel ->
                viewModel.updateTraveler(traveler)
            }
        }

        Observable.combineLatest(frequentFlyerCardsObservable, frequentFlyerPlans, { cards, plans ->
            viewHolderViewModels.clear()
            cards.forEach {
                val viewModel = FlightTravelerFrequentFlyerItemViewModel(traveler)
                setUpFrequentFlyerPlans(plans, viewModel)
                viewHolderViewModels.add(viewModel)
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