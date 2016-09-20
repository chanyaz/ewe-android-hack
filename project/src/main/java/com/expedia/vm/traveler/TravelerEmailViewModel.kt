package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

open class TravelerEmailViewModel(val context: Context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private var traveler: Traveler? = null

    val emailAddressSubject = BehaviorSubject.create<String>()
    val emailErrorSubject = PublishSubject.create<Boolean>()

    val emailAddressObserver = endlessObserver<String>() { email ->
        traveler?.email = email
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    fun updateEmail(traveler: Traveler) {
        this.traveler = traveler
        emailAddressSubject.onNext(if (traveler.email.isNullOrEmpty()) "" else traveler.email)
    }

    open fun validate(): Boolean {
        val validEmail = travelerValidator.isValidEmail(traveler?.email)
        emailErrorSubject.onNext(!validEmail)
        return validEmail
    }
}