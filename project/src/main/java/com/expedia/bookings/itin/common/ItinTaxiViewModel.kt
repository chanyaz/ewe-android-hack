package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

abstract class ItinTaxiViewModel {
    val localizedLocationNameSubject: PublishSubject<String> = PublishSubject.create()
    val localizedAddressSubject: PublishSubject<String> = PublishSubject.create()
    val nonLocalizedLocationNameSubject: PublishSubject<String> = PublishSubject.create()
    val nonLocalizedAddressSubject: PublishSubject<String> = PublishSubject.create()
}
