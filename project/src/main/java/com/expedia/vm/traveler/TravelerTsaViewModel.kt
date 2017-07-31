package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import javax.inject.Inject

class TravelerTSAViewModel(val traveler: Traveler, val context: Context, val lob: LineOfBusiness) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val dateOfBirthViewModel = DateOfBirthViewModel(traveler, context, lob)
    val genderViewModel = GenderViewModel(traveler, context)

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        updateTraveler(traveler)
    }

    fun updateTraveler(traveler: Traveler) {
        dateOfBirthViewModel.updateTravelerBirthDate(traveler)
        genderViewModel.updateTravelerGender(traveler)
    }

    fun validate(): Boolean {
        val validBirthDate = dateOfBirthViewModel.validate()
        val validGender = genderViewModel.validate()
        return validBirthDate && validGender
    }
}
