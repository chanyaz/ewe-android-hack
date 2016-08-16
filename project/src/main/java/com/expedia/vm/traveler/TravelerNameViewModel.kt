package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

class TravelerNameViewModel(context: Context): InvalidCharacterHelper.InvalidCharacterListener {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private var travelerName: TravelerName by Delegates.notNull()

    val firstNameSubject = BehaviorSubject.create<String>()
    val middleNameSubject = BehaviorSubject.create<String>()
    val lastNameSubject = BehaviorSubject.create<String>()

    val fullNameSubject = BehaviorSubject.create<String>()

    val firstNameObserver = endlessObserver<String>() {
        travelerName.firstName = it
        nameUpdated()
    }

    val middleNameObserver = endlessObserver<String>() {
        travelerName.middleName = it
        nameUpdated()
    }

    val lastNameObserver = endlessObserver<String>() {
        travelerName.lastName = it
        nameUpdated()
    }

    val firstNameErrorSubject = PublishSubject.create<Boolean>()
    val middleNameErrorSubject = PublishSubject.create<Boolean>()
    val lastNameErrorSubject = PublishSubject.create<Boolean>()

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    fun updateTravelerName(travelerName: TravelerName) {
        this.travelerName = travelerName
        firstNameSubject.onNext(if (travelerName.firstName.isNullOrEmpty()) "" else travelerName.firstName)
        middleNameSubject.onNext(if (travelerName.middleName.isNullOrEmpty()) "" else travelerName.middleName)
        lastNameSubject.onNext(if (travelerName.lastName.isNullOrEmpty()) "" else travelerName.lastName)
        fullNameSubject.onNext(if (travelerName.fullName.isNullOrEmpty()) "" else travelerName.fullName)
    }

    private fun nameUpdated() {
        fullNameSubject.onNext(travelerName.fullName)
    }

    fun validate(): Boolean {
        travelerValidator.isRequiredNameValid(travelerName.firstName)
        val firstNameValid = travelerValidator.isRequiredNameValid(travelerName.firstName)
        firstNameErrorSubject.onNext(!firstNameValid)

        val middleNameValid = travelerValidator.isMiddleNameValid(travelerName.middleName)
        middleNameErrorSubject.onNext(!middleNameValid)

        val lastNameValid = travelerValidator.isLastNameValid(travelerName.lastName)
        lastNameErrorSubject.onNext(!lastNameValid)

        return firstNameValid && middleNameValid && lastNameValid
    }

    override fun onInvalidCharacterEntered(text: CharSequence?, mode: InvalidCharacterHelper.Mode?) {
        // TODO fix this to match old behavior
        throw UnsupportedOperationException()
    }
}