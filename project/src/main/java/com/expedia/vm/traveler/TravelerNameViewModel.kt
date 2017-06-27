package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.utils.AccessibilityUtil
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

open class TravelerNameViewModel(context: Context) {
    private var travelerName: TravelerName by Delegates.notNull()
    val firstNameViewModel = FirstNameViewModel(context)
    val middleNameViewModel = MiddleNameViewModel(context)
    val lastNameViewModel = LastNameViewModel(context)

    val fullNameSubject = BehaviorSubject.create<String>()
    var numberOfInvalidFields = BehaviorSubject.create<Int>()

    init {
        firstNameViewModel.textSubject.subscribe {
            travelerName.firstName = it
            nameUpdated()
        }

        middleNameViewModel.textSubject.subscribe {
            travelerName.middleName = it
            nameUpdated()
        }

        lastNameViewModel.textSubject.subscribe {
            travelerName.lastName = it
            nameUpdated()
        }
    }

    fun updateTravelerName(travelerName: TravelerName) {
        this.travelerName = travelerName
        firstNameViewModel.textSubject.onNext(if (travelerName.firstName.isNullOrEmpty()) "" else travelerName.firstName)
        middleNameViewModel.textSubject.onNext(if (travelerName.middleName.isNullOrEmpty()) "" else travelerName.middleName)
        lastNameViewModel.textSubject.onNext(if (travelerName.lastName.isNullOrEmpty()) "" else travelerName.lastName)
        fullNameSubject.onNext(if (travelerName.fullName.isNullOrEmpty()) "" else travelerName.fullName)
    }

    private fun nameUpdated() {
        fullNameSubject.onNext(travelerName.fullName)
    }

    open fun validate(): Boolean {
        val firstNameValid = firstNameViewModel.validate()
        val middleNameValid = middleNameViewModel.validate()
        val lastNameValid = lastNameViewModel.validate()

        numberOfInvalidFields.onNext(AccessibilityUtil.getNumberOfInvalidFields(firstNameValid, middleNameValid,lastNameValid))
        return firstNameValid && middleNameValid && lastNameValid
    }
}