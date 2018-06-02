package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

interface ICustomerSupportViewModel {
    val customerSupportHeaderTextSubject: PublishSubject<String>
    val phoneNumberSubject: PublishSubject<String>
    val phoneNumberClickedSubject: PublishSubject<Unit>
    val customerSupportTextSubject: PublishSubject<String>
    val customerSupportButtonClickedSubject: PublishSubject<Unit>
    val itineraryNumberSubject: PublishSubject<String>
    val itineraryHeaderVisibilitySubject: PublishSubject<Boolean>
    val phoneNumberContentDescriptionSubject: PublishSubject<String>
    val customerSupportTextContentDescriptionSubject: PublishSubject<String>
    val itineraryNumberContentDescriptionSubject: PublishSubject<String>
}
