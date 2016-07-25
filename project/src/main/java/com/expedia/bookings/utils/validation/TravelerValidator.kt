package com.expedia.bookings.utils.validation

import android.text.TextUtils
import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError
import org.joda.time.LocalDate

class TravelerValidator {
    private var startOfTrip: LocalDate? = null
    private var endOfTrip: LocalDate? = null
    private var infantsInLap: Boolean = false

    fun updateForNewSearch(params : AbstractFlightSearchParams) {
        startOfTrip = params.startDate
        endOfTrip = params.getEndOfTripDate()
        infantsInLap = params.infantSeatingInLap
    }

    fun isValidForPackageBooking(traveler: Traveler): Boolean {
        return hasValidBirthDate(traveler) && hasValidName(traveler.name)
                && isValidPhone(traveler.phoneNumber) && hasValidGender(traveler)
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

    fun hasValidBirthDate(traveler: Traveler): Boolean {
        val birthDate = traveler.birthDate
        if (birthDate != null) {
            if (birthDate.isAfter(LocalDate.now())) {
                return false
            } else if (!validatePassengerCategory(traveler.birthDate, traveler.passengerCategory)) {
                return false
            }
        } else {
            return false
        }
        return true
    }

    fun validatePassengerCategory(birthDate: LocalDate?, category: PassengerCategory?) : Boolean {
        if (startOfTrip == null || endOfTrip == null) {
            throw RuntimeException("Error: Attempted to validate PassengerCategory before trip dates were properly initialized")
        }
        else if (birthDate == null || category == null) {
            return false
        } else {
            val inclusiveAgeBounds = PassengerCategory.getAcceptableAgeRange(category)

            val earliestBirthDateAllowed = (startOfTrip as LocalDate).minusYears(inclusiveAgeBounds.second)
            val latestBirthDateAllowed = (endOfTrip as LocalDate).minusYears(inclusiveAgeBounds.first)

            val afterEarliest = birthDate.compareTo(earliestBirthDateAllowed) > 0
            val beforeLatest = birthDate.compareTo(latestBirthDateAllowed) <= 0

            return beforeLatest && afterEarliest
        }
    }
}