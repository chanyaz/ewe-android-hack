package com.expedia.bookings.utils

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import org.joda.time.LocalDate

class TravelerTestUtils {

    companion object {
        fun completeTraveler(traveler: Traveler) {
            traveler.age = 40
            traveler.gender = Traveler.Gender.MALE
            traveler.email = "12345@aol.com"
            traveler.birthDate = LocalDate.now().minusYears(40)
            traveler.firstName = "Oscar"
            traveler.lastName = "Grouch"
            traveler.tuid = 987654321
            traveler.phoneNumber = "4053615992"
            traveler.passengerCategory = PassengerCategory.ADULT
        }
    }
}
