package com.expedia.bookings.itin.flight.traveler

import com.expedia.bookings.data.Traveler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightItinTravelerInfoViewModel {
    val travelerObservable: BehaviorSubject<Traveler> = BehaviorSubject.create()
    val travelerNameSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val ticketNumberSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val travelerEmailSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val travelerPhoneSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val infantInLapSubject: PublishSubject<CharSequence> = PublishSubject.create()
}
