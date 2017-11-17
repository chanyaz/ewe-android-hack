package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightItinTravelerPreferenceViewModel {
    val travelerObservable: BehaviorSubject<Traveler> = BehaviorSubject.create()
    val knownTravelerNumberSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val redressNumberSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val frequentFlyerSubject: PublishSubject<Map<String, TravelerFrequentFlyerMembership>> = PublishSubject.create()
    val specialRequestSubject: PublishSubject<CharSequence> = PublishSubject.create()
}