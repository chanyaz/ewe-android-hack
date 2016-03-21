package com.expedia.vm.traveler

import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class TravelerNameViewModel(): InvalidCharacterHelper.InvalidCharacterListener {
    private var travelerName: TravelerName by Delegates.notNull()

    val firstNameSubject = BehaviorSubject.create<String>()
    val middleNameSubject = BehaviorSubject.create<String>()
    val lastNameSubject = BehaviorSubject.create<String>()

    val fullNameSubject = BehaviorSubject.create<String>()

    val firstNameObserver = endlessObserver<TextViewAfterTextChangeEvent>() { name ->
        travelerName.firstName = name.editable().toString()
        nameUpdated()
    }

    val middleNameObserver = endlessObserver<TextViewAfterTextChangeEvent>() { name ->
        travelerName.middleName = name.editable().toString()
        nameUpdated()
    }

    val lastNameObserver = endlessObserver<TextViewAfterTextChangeEvent>() { name ->
        travelerName.lastName = name.editable().toString()
        nameUpdated()
    }

    val firstNameErrorSubject = PublishSubject.create<Boolean>()
    val middleNameErrorSubject = PublishSubject.create<Boolean>()
    val lastNameErrorSubject = PublishSubject.create<Boolean>()

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
        TravelerValidator.isRequiredNameValid(travelerName.firstName)
        val firstNameValid = TravelerValidator.isRequiredNameValid(travelerName.firstName)
        firstNameErrorSubject.onNext(!firstNameValid)

        val middleNameValid = TravelerValidator.isMiddleNameValid(travelerName.middleName)
        middleNameErrorSubject.onNext(!middleNameValid)

        val lastNameValid = TravelerValidator.isRequiredNameValid(travelerName.lastName)
        lastNameErrorSubject.onNext(!lastNameValid)

        return firstNameValid && middleNameValid && lastNameValid
    }

    override fun onInvalidCharacterEntered(text: CharSequence?, mode: InvalidCharacterHelper.Mode?) {
        // TODO fix this to match old behavior
        throw UnsupportedOperationException()
    }
}