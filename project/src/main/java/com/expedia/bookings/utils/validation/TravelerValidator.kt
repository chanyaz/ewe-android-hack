package com.expedia.bookings.utils.validation

import android.text.TextUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError
import org.joda.time.LocalDate

object TravelerValidator {
    fun isValidForPackageBooking(traveler: Traveler): Boolean {
        return hasValidBirthDate(traveler) && hasValidName(traveler.getName()) && isValidPhone(traveler.phoneNumber) && hasValidGender(traveler)
    }

    fun hasValidBirthDate(traveler: Traveler): Boolean {
        val searchParams = Db.getPackageParams()
        val birthDate = traveler.birthDate
        if (birthDate!= null) {
            val passengerCategory = traveler.getPassengerCategory(searchParams)

            if (birthDate.isAfter(LocalDate.now())) {
                return false
            } else if (!PassengerCategory.isDateWithinPassengerCategoryRange(birthDate, searchParams, passengerCategory)) {
                return false
            }
        } else {
            return false
        }
        return true
    }

    fun hasValidGender(traveler: Traveler): Boolean {
        if (traveler.gender == Traveler.Gender.GENDER) {
            return false
        } else {
            return true
        }
    }

    fun isValidPhone(number: String?): Boolean {
        return CommonSectionValidators.TELEPHONE_NUMBER_VALIDATOR_STRING.validate(number) == ValidationError.NO_ERROR
    }

    fun isRequiredNameValid(name: String?) : Boolean {
        return CommonSectionValidators.NON_EMPTY_VALIDATOR.validate(name) == ValidationError.NO_ERROR
                && hasAllValidChars(name)
    }

    fun isLastNameValid(name: String?) : Boolean {
        return isRequiredNameValid(name) && name?.length ?: 0 >= 2
    }

    fun isMiddleNameValid(name: String?) : Boolean {
        return hasAllValidChars(name)
    }

    fun hasAllValidChars(name: String?): Boolean {
        if (name == null) {
            return true
        }
        return CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING.validate(name) == ValidationError.NO_ERROR
    }

    fun hasValidName(name: TravelerName): Boolean {
        val validFirstName = isRequiredNameValid(name.firstName)
        val validMiddleName = isMiddleNameValid(name.middleName)
        val validLastName = isLastNameValid(name.lastName)

        return validFirstName && validMiddleName && validLastName
    }

    fun isTravelerEmpty(traveler: Traveler) : Boolean {
        return traveler.name.isEmpty && TextUtils.isEmpty(traveler.phoneNumber) && traveler.birthDate == null
    }
}