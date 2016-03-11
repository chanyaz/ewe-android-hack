package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerTSAViewModel(val context: Context, var traveler: Traveler) {
    val defaultDateSubject = BehaviorSubject.create<LocalDate>(LocalDate(1970, 1, 1))

    val formattedDateSubject = BehaviorSubject.create<String>()
    val birthDateSubject = BehaviorSubject.create<LocalDate>()
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()

    val dateOfBirthErrorSubject = PublishSubject.create<Boolean>()

    init {
        val date = traveler.birthDate
        if (date != null) {
            birthDateSubject.onNext(date)
            formattedDateSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
        } else {
            formattedDateSubject.onNext("")
        }
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
    }

    val dateOfBirthObserver = endlessObserver<LocalDate> { date ->
        traveler.birthDate = date
        birthDateSubject.onNext(date)
        formattedDateSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
    }

    val genderObserver = endlessObserver<Traveler.Gender> { gender ->
        traveler.gender = gender
        genderSubject.onNext(gender)
    }

    fun validate(): Boolean {
        val validBirthDate = TravelerValidator.hasValidBirthDate(traveler)
        dateOfBirthErrorSubject.onNext(!validBirthDate)
        return validBirthDate
    }
}
