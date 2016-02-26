package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class NameEntryViewModel(var traveler: Traveler): InvalidCharacterHelper.InvalidCharacterListener {
    val firstNameSubject = BehaviorSubject.create<String>()
    val middleNameSubject = BehaviorSubject.create<String>()
    val lastNameSubject = BehaviorSubject.create<String>()

    val firstNameObserver = endlessObserver<String> { name ->
        traveler.firstName = name
    }
    val middleNameObserver = endlessObserver<String> { name ->
        traveler.middleName = name
    }
    val lastNameObserver = endlessObserver<String> { name ->
        traveler.lastName = name
    }

    val firstNameErrorSubject = PublishSubject.create<Int>()
    val middleNameErrorSubject = PublishSubject.create<Int>()
    val lastNameErrorSubject = PublishSubject.create<Int>()

    init {
        firstNameSubject.onNext(if (traveler.firstName.isNullOrEmpty()) "" else traveler.firstName )
        middleNameSubject.onNext(if (traveler.middleName.isNullOrEmpty()) "" else traveler.middleName)
        lastNameSubject.onNext(if (traveler.lastName.isNullOrEmpty()) "" else traveler.lastName)
    }

    fun validate(): Boolean {
        val firstNameValid = validateName(traveler.firstName, firstNameErrorSubject, true)
        val middleNameValid = validateName(traveler.middleName, middleNameErrorSubject, false)
        val lastNameValid = validateName(traveler.lastName, lastNameErrorSubject, true)

        return firstNameValid && middleNameValid && lastNameValid
    }

    fun validateName(name: String?, errorSubject: PublishSubject<Int>, required: Boolean): Boolean {
        val emptyError = CommonSectionValidators.NON_EMPTY_VALIDATOR.validate(name)
        if (emptyError != 0) {
            if (required) {
                errorSubject.onNext(emptyError)
                return false
            } else {
                return true
            }
        }
        val invalidCharError = CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING.validate(name)
        if (invalidCharError != 0) {
            errorSubject.onNext(invalidCharError)
            return false
        }
        return true
    }

    override fun onInvalidCharacterEntered(text: CharSequence?, mode: InvalidCharacterHelper.Mode?) {
        // TODO fix this to match old behavior
        throw UnsupportedOperationException()
    }
}