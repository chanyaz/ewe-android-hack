package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

interface IMoreHelpViewModel {
    val phoneNumberSubject: PublishSubject<String>
    val callButtonContentDescriptionSubject: PublishSubject<String>
    val helpTextSubject: PublishSubject<String>
    val confirmationNumberSubject: PublishSubject<String>
    val confirmationTitleVisibilitySubject: PublishSubject<Boolean>
    val confirmationNumberContentDescriptionSubject: PublishSubject<String>
    val phoneNumberClickSubject: PublishSubject<Unit>
}
