package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.utils.validation.TravelerValidator
import rx.subjects.BehaviorSubject

class GenderViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()
    val materialFormTestEnabled = isMaterialFormsEnabled()

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ANY

    private val travelerValidator: TravelerValidator = Ui.getApplication(context).travelerComponent().travelerValidator()

    init {
        genderSubject.subscribe { gender ->
            traveler.gender = gender
        }

        if (!materialFormTestEnabled) {
            errorSubject.onNext(false)
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