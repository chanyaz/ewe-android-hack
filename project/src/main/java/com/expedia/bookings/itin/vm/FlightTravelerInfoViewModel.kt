package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightTravelerInfoViewModel {
    val travelerObservable: BehaviorSubject<Traveler> = BehaviorSubject.create()
    val travelerNameSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val ticketNumberSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val travelerEmailSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val travelerPhoneSubject: PublishSubject<CharSequence> = PublishSubject.create()
    val infantInLapSubject: PublishSubject<CharSequence> = PublishSubject.create()
}