package com.expedia.bookings.test.utils.validation

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.validation.TravelerValidator
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerValidatorTest {
    val mainTravelerIndex = 0
    val addTravelerIndex = 1
    val validFirstName = "Oscar"
    val validMiddleName = "T"
    val validLastName = "Grouch"
    val TEST_EMAIL = "test@gmail.com"
    val TEST_NUMBER = " 773 202 5862"

    val TOMORROW = LocalDate.now().plusDays(1)

    val travelerValidator = TravelerValidator()

    @Test
    fun testInvalidChar() {
        assertFalse(travelerValidator.hasAllValidChars("%@#$@!"))
    }

    @Test
    fun testEmptyInvalidChar() {
        assertTrue(travelerValidator.hasAllValidChars(""), "Expected No Chars = all valid chars")
        assertTrue(travelerValidator.hasAllValidChars(null), "Expected No Chars = all valid chars")
    }

    @Test
    fun testValidChars() {
        assertTrue(travelerValidator.hasAllValidChars(validFirstName))
    }

    @Test
    fun testEmptyName() {
        assertFalse(travelerValidator.isRequiredNameValid(null))
        assertFalse(travelerValidator.isRequiredNameValid(""))
    }

    @Test
    fun testValidRequiredName() {
        assertTrue(travelerValidator.isRequiredNameValid(validFirstName))
    }

    @Test
    fun testFirstNameRequired() {
        val noFirstName = TravelerName()
        noFirstName.middleName = validMiddleName
        noFirstName.lastName = validLastName

        assertFalse(travelerValidator.hasValidName(noFirstName))
    }

    @Test
    fun testLastNameRequired() {
        val noLastName = TravelerName()
        noLastName.firstName = validFirstName
        noLastName.middleName = validMiddleName

        assertFalse(travelerValidator.hasValidName(noLastName))
    }

    @Test
    fun testStillValidWithNoMiddleName() {
        val validName = TravelerName()
        validName.firstName = validFirstName
        validName.lastName = validLastName

        assertTrue(travelerValidator.hasValidName(validName))
    }

    @Test
    fun testValidName() {
        assertTrue(travelerValidator.hasValidName(getValidName()))
    }

    @Test
    fun testInValidLastName() {
        assertFalse(travelerValidator.isLastNameValid(""))
        assertFalse(travelerValidator.isLastNameValid("a"))
        assertFalse(travelerValidator.isLastNameValid("1"))
        assertFalse(travelerValidator.isLastNameValid("a1"))
        assertTrue(travelerValidator.isLastNameValid("Sn"))
    }

    @Test
    fun testEmailValidator() {
        assertFalse(travelerValidator.isValidEmail("qa-ehcc"))
        assertFalse(travelerValidator.isValidEmail("qa-ehcc@"))
        assertFalse(travelerValidator.isValidEmail("qa-ehcc@mobiata"))
        assertTrue(travelerValidator.isValidEmail("TEST@email.com"))
        assertTrue(travelerValidator.isValidEmail("test@email.com"))
        assertFalse(travelerValidator.isValidEmail("qa-ehcc"))
    }

    @Test
    fun invalidPhone() {
        assertFalse(travelerValidator.isValidPhone("12"))
    }

    @Test
    fun emptyPhone() {
        assertFalse(travelerValidator.isValidPhone(null))
        assertFalse(travelerValidator.isValidPhone(""))
    }

    @Test
    fun validPhone() {
        assertTrue(travelerValidator.isValidPhone(TEST_NUMBER))
    }

    @Test
    fun invalidFutureBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val invalidBirthDate = LocalDate.now().plusDays(2)
        val mockTraveler = getMockAdultTravelerWithBirthDate(invalidBirthDate)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateEmpty() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val mockTraveler = getMockAdultTravelerWithBirthDate(null)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateToYoung() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val youngBirthDate = TOMORROW.minusYears(8)
        val mockTraveler = getMockAdultTravelerWithBirthDate(youngBirthDate)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be to young to be considered an adult, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun validBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be valid adult," +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange" +
                "if this tests fails look there as well")
    }

    @Test
    fun testInvalidForPackageNoName() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoPhone() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoPassport() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val mockTraveler = givenTravelerOnlyMissingPassport()
        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, true))
    }

    @Test
    fun testValidMainForPackageBookingPassportNeeded() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val mockTraveler = givenTravelerOnlyMissingPassport()
        Mockito.`when`(mockTraveler.primaryPassportCountry).thenReturn("Mexico")
        
        assertTrue(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, true))
    }

    @Test
    fun testValidMainForPackageBooking() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))

        Mockito.`when`(mockTraveler.email).thenReturn(TEST_EMAIL)
        assertFalse(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))

        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        assertTrue(travelerValidator.isValidForBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testValidAddForPackageBooking() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertTrue(travelerValidator.isValidForBooking(mockTraveler, addTravelerIndex, false))
    }

    @Test
    fun testIsEmptyWithName() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(travelerValidator.isTravelerEmpty(mockTraveler))
    }

    @Test
    fun testIsEmptyWithPhone() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(travelerValidator.isTravelerEmpty(mockTraveler))
    }


    @Test
    fun testIsEmptyWithBirthDate() {
        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(travelerValidator.isTravelerEmpty(mockTraveler))
    }

    @Test
    fun testIsEmpty() {
        assertTrue(travelerValidator.isTravelerEmpty(Traveler()))
    }

    private fun getValidName(): TravelerName {
        val validName = TravelerName()
        validName.firstName = validFirstName
        validName.middleName = validMiddleName
        validName.lastName = validLastName

        return validName
    }

    private fun getMockAdultTravelerWithBirthDate(birthDate : LocalDate?) : Traveler {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.birthDate).thenReturn(birthDate)
        Mockito.`when`(mockTraveler.passengerCategory).thenReturn(PassengerCategory.ADULT)

        return mockTraveler
    }

    private fun getInstanceOfPackageSearchParams(checkIn: LocalDate, checkOut: LocalDate) : PackageSearchParams {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(checkIn)
                .endDate(checkOut)
                .origin(SuggestionV4())
                .destination(SuggestionV4())
        .build() as PackageSearchParams
        return packageParams
    }

    private fun givenTravelerOnlyMissingPassport() : Traveler {
        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)

        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        Mockito.`when`(mockTraveler.email).thenReturn(TEST_EMAIL)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)

        return mockTraveler
    }
}