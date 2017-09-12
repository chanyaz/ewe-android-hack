package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilyDetails
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class FlightFareFamilyViewModel(val context: Context) {
    val tripObservable = PublishSubject.create<FlightTripResponse>()
    val showFareFamilyObservable = PublishSubject.create<Unit>()
    val fareFamilyDetailsObservable = PublishSubject.create<Array<FareFamilyDetails>>()
    val selectedFareFamilyObservable = PublishSubject.create<FareFamilyDetails>()
    val choosingFareFamilyObservable = PublishSubject.create<FareFamilyDetails>()
    val doneButtonObservable = PublishSubject.create<Unit>()
    var fareFamilyTripLocationObservable = PublishSubject.create<String>()
    var roundTripObservable = PublishSubject.create<Boolean>()
    var airlinesObservable = PublishSubject.create<String>()

    init {
        showFareFamilyObservable.withLatestFrom(tripObservable, { showFareFamily, trip ->
            trip
        }).subscribe { trip ->
            trip.fareFamilyList?.fareFamilyDetails?.let {
                val defaultFareFamily = it.firstOrNull()
                if (!trip.isFareFamilyUpgraded && defaultFareFamily != null) {
                    selectedFareFamilyObservable.onNext(defaultFareFamily)
                    choosingFareFamilyObservable.onNext(defaultFareFamily)
                }
                fareFamilyDetailsObservable.onNext(it)
                fareFamilyTripLocationObservable.onNext(getFareFamilyTripLocation())
                roundTripObservable.onNext(isRoundTrip())
                airlinesObservable.onNext(getAirlinesString(trip))
            }
        }
        doneButtonObservable.withLatestFrom(choosingFareFamilyObservable, {click, chosenFareFamily -> chosenFareFamily}).subscribe {
            selectedFareFamilyObservable.onNext(it)
        }
    }

    fun getFareFamilyTripLocation() : String {
        val flightSearchParams = Db.getFlightSearchParams()
        var phrase: Phrase? = null

        if(flightSearchParams.isRoundTrip()) {
            phrase = Phrase.from(context, R.string.flight_departure_arrival_code_round_trip_TEMPLATE)
        }
        else {
            phrase = Phrase.from(context, R.string.flight_departure_arrival_code_one_way_TEMPLATE)
        }
        return phrase.put("departure_code", flightSearchParams.departureAirport.hierarchyInfo?.airport?.airportCode)
                .put("arrival_code", flightSearchParams.arrivalAirport.hierarchyInfo?.airport?.airportCode)
                .format().toString()
    }

    fun isRoundTrip() : Boolean {
        return Db.getFlightSearchParams().isRoundTrip()
    }

    fun getAirlinesString(trip: FlightTripResponse) : String {
        val airlines = trip.details.getLegs().flatMap { it -> it.segments.map { segment -> segment.airlineName } }.distinct()
        if (airlines.size > 3) {
            return Phrase.from(context, R.string.multiple_carriers_text).format().toString()
        }
        return airlines.joinToString(Phrase.from(context, R.string.flight_airline_names_delimiter).format().toString())
    }
}
