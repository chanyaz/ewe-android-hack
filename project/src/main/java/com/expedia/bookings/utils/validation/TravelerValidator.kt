package com.expedia.bookings.utils.validation

import android.text.TextUtils
import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerUtils
import com.mobiata.android.validation.ValidationError
import org.joda.time.LocalDate

class TravelerValidator(private val userStateManager: UserStateManager) {
    private var startOfTrip: LocalDate? = null
    private var endOfTrip: LocalDate? = null
    private var infantsInLap: Boolean = false

    fun updateForNewSearch(params : AbstractFlightSearchParams) {
        startOfTrip = params.startDate
        endOfTrip = params.getEndOfTripDate()
        infantsInLap = params.infantSeatingInLap
    }

    fun isValidForFlightBooking(traveler: Traveler, index: Int, passportRequired: Boolean): Boolean {
        return hasValidBirthDate(traveler) && hasValidName(traveler.name) && hasValidGender(traveler)
                && (!TravelerUtils.isMainTraveler(index) || isValidPhone(traveler.phoneNumber))
                && (userStateManager.isUserAuthenticated() || !TravelerUtils.isMainTraveler(index) || isValidEmail(traveler.email))
                && (!passportRequired || hasValidPassport(traveler))
    }

    fun isValidForRailBooking(traveler: Traveler) : Boolean {
        return hasValidTravelerDetails(traveler)
    }

    private fun hasValidTravelerDetails(traveler: Traveler): Boolean {
        return hasValidName(traveler.name)
                && isValidPhone(traveler.phoneNumber)
                && isValidEmail(traveler.email)
    }

    fun isValidForHotelBooking(traveler: Traveler) : Boolean {
        return hasValidTravelerDetails(traveler)
    }

    fun hasValidGender(traveler: Traveler): Boolean {
        return traveler.gender == Traveler.Gender.FEMALE || traveler.gender == Traveler.Gender.MALE
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

    fun isValidEmail(email: String?): Boolean {
        return CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == ValidationError.NO_ERROR
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

    fun hasValidPassport(traveler: Traveler) : Boolean {
        return Strings.isNotEmpty(traveler.primaryPassportCountry)
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
            return PassengerCategory.isDateWithinPassengerCategoryRange(birthDate, endOfTrip, startOfTrip, category)
        }
    }
}