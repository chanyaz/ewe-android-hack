package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject

class DateOfBirthViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {

    val travelerValidator: TravelerValidator by lazy {
        val validator = Ui.getApplication(context).travelerComponent().travelerValidator()
        validator
    }

    val birthDateSubject = BehaviorSubject.create<LocalDate>()
    val defaultDateSubject = BehaviorSubject.create<LocalDate>(LocalDate(1970, 1, 1))
    val birthErrorTextSubject = BehaviorSubject.create<String>()

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ANY

    init {
        updateTravelerBirthDate(traveler)
    }

    fun updateTravelerBirthDate(traveler: Traveler) {
        this.traveler = traveler
        val date = traveler.birthDate
        if (date != null) {
            birthDateSubject.onNext(date)
            textSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
            validatePassengerCategory()
        } else {
            textSubject.onNext("")
        }
    }

    override fun isValid(): Boolean {
        return travelerValidator.hasValidBirthDate(traveler)
    }

    val dateOfBirthObserver = endlessObserver<LocalDate> { date ->
        traveler.birthDate = date
        birthDateSubject.onNext(date)
        textSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
        validatePassengerCategory()
    }

    fun validatePassengerCategory() {
        val validBirthDate = travelerValidator.hasValidBirthDate(traveler)
        if (validBirthDate) {
            return
        }

        val category = traveler.passengerCategory

        if (category == PassengerCategory.INFANT_IN_LAP || category == PassengerCategory.INFANT_IN_SEAT) {
            birthErrorTextSubject.onNext(context.getString(R.string.traveler_infant_error))
        } else if (category == PassengerCategory.CHILD) {
            birthErrorTextSubject.onNext(context.getString(R.string.traveler_child_error))
        } else if (category == PassengerCategory.ADULT_CHILD) {
            birthErrorTextSubject.onNext(context.getString(R.string.traveler_adult_child_error))
        } else {
            birthErrorTextSubject.onNext(context.getString(R.string.traveler_adult_error))
        }
        errorSubject.onNext(true)
    }
}