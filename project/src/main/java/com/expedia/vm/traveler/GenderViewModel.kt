package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import io.reactivex.subjects.BehaviorSubject

class GenderViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()
    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ANY

    private val travelerValidator: TravelerValidator = Ui.getApplication(context).travelerComponent().travelerValidator()

    init {
        genderSubject.subscribe { gender ->
            traveler.gender = gender
        }
    }

    fun updateTravelerGender(traveler: Traveler) {
        this.traveler = traveler
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
    }

    override fun isValid(): Boolean {
        return travelerValidator.hasValidGender(traveler)
    }
}
