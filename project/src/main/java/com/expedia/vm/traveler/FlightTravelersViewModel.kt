package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.enums.TravelerCheckoutStatus
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightTravelersViewModel(context: Context, lob: LineOfBusiness, showMainTravelerMinAgeMessaging: Boolean) : TravelersViewModel(context, lob, showMainTravelerMinAgeMessaging) {
    override fun createNewTravelerEntryWidgetModel(context: Context, index: Int, passportRequired: BehaviorSubject<Boolean>, currentStatus: TravelerCheckoutStatus): AbstractUniversalCKOTravelerEntryWidgetViewModel {
        return FlightTravelerEntryWidgetViewModel(context, index, passportRequired, currentStatus)
    }

    override fun requiresMultipleTravelers() = getTravelers().size > 1

    val flightOfferObservable = PublishSubject.create<FlightTripDetails.FlightOffer>()
    var flightLegs: List<FlightLeg> ?= null
    var frequentFlyerPlans : FlightCreateTripResponse.FrequentFlyerPlans ?= null

    init{
        flightOfferObservable.map { it.isPassportNeeded || it.isInternational }.subscribe(passportRequired)
    }

    override fun getTravelers() : List<Traveler> {
        return Db.getTravelers()
    }
}
