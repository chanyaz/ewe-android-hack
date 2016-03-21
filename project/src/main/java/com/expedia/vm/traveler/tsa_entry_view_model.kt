package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.validation.ValidationError
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.Calendar

class TSAEntryViewModel(val context: Context, var traveler: Traveler) {
    val defaultDateSubject = BehaviorSubject.create<LocalDate>(LocalDate(1970, 1, 1))

    val formattedDateSubject = BehaviorSubject.create<String>()
    val birthDateSubject = BehaviorSubject.create<LocalDate>()
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()

    val dateOfBirthErrorSubject = PublishSubject.create<Int>()

    init {
        val date = traveler.birthDate
        if (date != null) {
            birthDateSubject.onNext(date)
            formattedDateSubject.onNext(DateFormatUtils.formatBirthDate(context, date.year, date.monthOfYear, date.dayOfMonth))
        }
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
    }

    val dateOfBirthObserver = endlessObserver<LocalDate> { date ->
        traveler.birthDate = date
        birthDateSubject.onNext(date)
    }

    val genderObserver = endlessObserver<Traveler.Gender> { gender ->
        traveler.gender = gender
    }

    fun validate(): Boolean {
        val searchParams = Db.getPackageParams()

        if (traveler.birthDate != null) {
            val passengerCategory = traveler.getPassengerCategory(Db.getPackageParams())

            if (traveler.birthDate.isAfter(LocalDate.now())) {
                dateOfBirthErrorSubject.onNext(ValidationError.ERROR_DATA_INVALID);
                return false
            } else if (!PassengerCategory.isDateWithinPassengerCategoryRange(traveler.birthDate, searchParams, passengerCategory)) {
                dateOfBirthErrorSubject.onNext(ValidationError.ERROR_DATA_INVALID);
                return false
            }
        } else {
            dateOfBirthErrorSubject.onNext(ValidationError.ERROR_DATA_MISSING);
            return false
        }
        return true
    }
}
