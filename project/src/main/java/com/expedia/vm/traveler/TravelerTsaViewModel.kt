package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

class TravelerTSAViewModel(val context: Context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private var traveler: Traveler by Delegates.notNull()

    val defaultDateSubject = BehaviorSubject.create<LocalDate>(LocalDate(1970, 1, 1))

    val formattedDateSubject = BehaviorSubject.create<String>()
    val birthDateSubject = BehaviorSubject.create<LocalDate>()
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()

    val dateOfBirthErrorSubject = PublishSubject.create<Boolean>()
    val birthErrorTextSubject = PublishSubject.create<String>()
    val genderErrorSubject = BehaviorSubject.create<Boolean>()

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    fun updateTraveler(traveler: Traveler) {
        this.traveler = traveler
        val date = traveler.birthDate
        if (date != null) {
            birthDateSubject.onNext(date)
            formattedDateSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
            validatePassengerCategory()
        } else {
            formattedDateSubject.onNext("")
        }
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
        genderErrorSubject.onNext(false)
    }

    val dateOfBirthObserver = endlessObserver<LocalDate> { date ->
        traveler.birthDate = date
        birthDateSubject.onNext(date)
        formattedDateSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
        validatePassengerCategory()
    }

    val genderObserver = endlessObserver<Traveler.Gender> { gender ->
        traveler.gender = gender
        genderSubject.onNext(gender)
    }

    fun validate(): Boolean {
        val validBirthDate = travelerValidator.hasValidBirthDate(traveler)
        val validGender = travelerValidator.hasValidGender(traveler)
        dateOfBirthErrorSubject.onNext(!validBirthDate)
        genderErrorSubject.onNext(!validGender)
        return validBirthDate && validGender
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
        dateOfBirthErrorSubject.onNext(true)
    }
}
