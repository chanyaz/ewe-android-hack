package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import rx.subjects.BehaviorSubject

class GenderViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()

    init {
        genderSubject.subscribe { gender ->
            traveler.gender = gender
        }

        errorSubject.onNext(false)
    }

    fun updateTravelerGender(traveler: Traveler) {
        this.traveler = traveler
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
    }

    override fun isValid(): Boolean {
        return traveler.gender != Traveler.Gender.GENDER
    }
}