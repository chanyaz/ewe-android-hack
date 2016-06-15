package com.expedia.vm.test.traveler

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito
import java.util.ArrayList

class MockTravelerProvider {
    val testFirstName = "Oscar"
    val testMiddleName = "The"
    val testLastName = "Grouch"

    val testFullName = "Oscar The Grouch"
    val testNumber = "773202LUNA"
    val adultBirthDate = LocalDate.now().minusYears(24)

    fun getCompleteMockTraveler(): Traveler {
        val mockPhone = Mockito.mock(Phone::class.java)
        Mockito.`when`(mockPhone.number).thenReturn(testNumber)

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidTravelerName())
        Mockito.`when`(mockTraveler.fullName).thenReturn(testFullName)
        Mockito.`when`(mockTraveler.primaryPhoneNumber).thenReturn(mockPhone)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(testNumber)

        Mockito.`when`(mockTraveler.birthDate).thenReturn(adultBirthDate)
        Mockito.`when`(mockTraveler.getPassengerCategory(Matchers.any<AbstractFlightSearchParams>())).thenReturn(PassengerCategory.ADULT)

        return mockTraveler
    }

    @Suppress("UNCHECKED_CAST")
    fun updateDBWithMockTravelers(travelerCount: Int, mockTraveler: Traveler) {
        val travelerList = ArrayList<Traveler>()
        for (i in 1..travelerCount) {
            travelerList.add(mockTraveler)
        }
        Db.setTravelers(travelerList as MutableList<Traveler>?)
    }

    fun addMockTravelerToDb(mockTraveler: Traveler) {
        val travelerList = Db.getTravelers()
        travelerList.add(mockTraveler)
    }

    private fun getValidTravelerName() : TravelerName {
        val name = TravelerName()
        name.firstName = testFirstName
        name.middleName = testMiddleName
        name.lastName = testLastName
        return name
    }
}