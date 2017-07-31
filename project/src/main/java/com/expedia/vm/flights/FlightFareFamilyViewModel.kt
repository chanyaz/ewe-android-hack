package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilyDetails
import com.expedia.bookings.data.flights.Airline
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class FlightFareFamilyViewModel(val context: Context) {
    val tripObservable = PublishSubject.create<FlightTripResponse>()
    val clickObservable = PublishSubject.create<Unit>()
    val fareFamilyDetailsObservable = PublishSubject.create<Array<FareFamilyDetails>>()
    val selectedFareFamilyObservable = BehaviorSubject.create<FareFamilyDetails>()
    val choosingFareFamilyObservable = BehaviorSubject.create<FareFamilyDetails>()
    val doneButtonObservable = PublishSubject.create<Unit>()
    var isFareFamilyUpgraded : Boolean = false //TODO this need to be updated with trip response
    var fareFamilyTripLocationObservable = PublishSubject.create<String>()
    var roundTripObservable = PublishSubject.create<Boolean>()
    var airlinesObservable = PublishSubject.create<String>()

    init {
        Observable.combineLatest(clickObservable, tripObservable, { click, trip ->
            if(!isFareFamilyUpgraded) {
                selectedFareFamilyObservable.onNext((trip.fareFamilies as FlightTripResponse.FareFamilies).fareFamilyDetails.get(0))
            }
            choosingFareFamilyObservable.onNext(selectedFareFamilyObservable.value)
            fareFamilyDetailsObservable.onNext((trip.fareFamilies as FlightTripResponse.FareFamilies).fareFamilyDetails)
            fareFamilyTripLocationObservable.onNext(getFareFamilyTripLocation())
            roundTripObservable.onNext(isRoundTrip())
            airlinesObservable.onNext(getAirlinesString(trip))

        }).subscribe()

        doneButtonObservable.subscribe {
            selectedFareFamilyObservable.onNext(choosingFareFamilyObservable.value)
            isFareFamilyUpgraded = true
        }

    }

    fun getFareFamilyTripLocation() : String {
        val flightSearchParams = Db.getFlightSearchParams()
        var phrase: Phrase = Phrase.from(context, R.string.flight_departure_arrival_code_one_way)

        if(flightSearchParams.isRoundTrip()) {
            phrase = Phrase.from(context, R.string.flight_departure_arrival_code_round_trip)
        }
        return phrase.put("departure_code", flightSearchParams.departureAirport.hierarchyInfo?.airport?.airportCode)
                .put("arrival_code", flightSearchParams.arrivalAirport.hierarchyInfo?.airport?.airportCode)
                .format().toString()
    }

    fun isRoundTrip() : Boolean {
        return Db.getFlightSearchParams().isRoundTrip()
    }

    fun getAirlinesString(trip: FlightTripResponse) : String {
        val airlines = trip.details.legs.flatMap { it -> it.segments.map { segment -> segment.airlineName } }.distinct()
        if (airlines.size > 3) {
            return Phrase.from(context, R.string.multiple_carriers_text).format().toString()
        }
        return airlines.joinToString(Phrase.from(context, R.string.flight_airline_names_delimiter).format().toString())
    }



}
