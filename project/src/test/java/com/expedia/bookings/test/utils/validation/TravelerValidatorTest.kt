package com.expedia.bookings.test.utils.validation

import com.expedia.bookings.data.Db
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
import org.mockito.Matchers.any
import org.mockito.Mockito
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerValidatorTest {
    val validFirstName = "Oscar"
    val validMiddleName = "T"
    val validLastName = "Grouch"

    val TEST_NUMBER = " 773 202 5862"

    val TOMORROW = LocalDate.now().plusDays(1)

    @Test
    fun testInvalidChar() {
        assertFalse(TravelerValidator.hasAllValidChars("%@#$@!"))
    }

    @Test
    fun testEmptyInvalidChar() {
        assertTrue(TravelerValidator.hasAllValidChars(""), "Expected No Chars = all valid chars")
        assertTrue(TravelerValidator.hasAllValidChars(null), "Expected No Chars = all valid chars")
    }

    @Test
    fun testValidChars() {
        assertTrue(TravelerValidator.hasAllValidChars(validFirstName))
    }

    @Test
    fun testEmptyName() {
        assertFalse(TravelerValidator.isRequiredNameValid(null))
        assertFalse(TravelerValidator.isRequiredNameValid(""))
    }

    @Test
    fun testValidRequiredName() {
        assertTrue(TravelerValidator.isRequiredNameValid(validFirstName))
    }

    @Test
    fun testFirstNameRequired() {
        val noFirstName = TravelerName()
        noFirstName.middleName = validMiddleName
        noFirstName.lastName = validLastName

        assertFalse(TravelerValidator.hasValidName(noFirstName))
    }

    @Test
    fun testLastNameRequired() {
        val noLastName = TravelerName()
        noLastName.firstName = validFirstName
        noLastName.middleName = validMiddleName

        assertFalse(TravelerValidator.hasValidName(noLastName))
    }

    @Test
    fun testStillValidWithNoMiddleName() {
        val validName = TravelerName()
        validName.firstName = validFirstName
        validName.lastName = validLastName

        assertTrue(TravelerValidator.hasValidName(validName))
    }

    @Test
    fun testValidName() {
        assertTrue(TravelerValidator.hasValidName(getValidName()))
    }

    @Test
    fun testInValidLastName() {
        assertFalse(TravelerValidator.isLastNameValid(""))
        assertFalse(TravelerValidator.isLastNameValid("a"))
        assertFalse(TravelerValidator.isLastNameValid("1"))
        assertFalse(TravelerValidator.isLastNameValid("a1"))
        assertTrue(TravelerValidator.isLastNameValid("Sn"))
    }

    @Test
    fun invalidPhone() {
        assertFalse(TravelerValidator.isValidPhone("12"))
    }

    @Test
    fun emptyPhone() {
        assertFalse(TravelerValidator.isValidPhone(null))
        assertFalse(TravelerValidator.isValidPhone(""))
    }

    @Test
    fun validPhone() {
        assertTrue(TravelerValidator.isValidPhone(TEST_NUMBER))
    }

    @Test
    fun invalidFutureBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val invalidBirthDate = LocalDate.now().plusDays(2)
        val mockTraveler = getMockAdultTravelerWithBirthDate(invalidBirthDate)

        assertFalse(TravelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateEmpty() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val mockTraveler = getMockAdultTravelerWithBirthDate(null)

        assertFalse(TravelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateToYoung() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val youngBirthDate = TOMORROW.minusYears(8)
        val mockTraveler = getMockAdultTravelerWithBirthDate(youngBirthDate)

        assertFalse(TravelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be to young to be considered an adult, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun validBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)

        assertTrue(TravelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be valid adult," +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange" +
                "if this tests fails look there as well")
    }

    @Test
    fun testInvalidForPackageNoName() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(TravelerValidator.isValidForPackageBooking(mockTraveler))
    }

    @Test
    fun testInvalidForPackageNoBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(TravelerValidator.isValidForPackageBooking(mockTraveler))
    }

    @Test
    fun testInvalidForPackageNoPhone() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(TravelerValidator.isValidForPackageBooking(mockTraveler))
    }

    @Test
    fun testValidForPacakgeBooking() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        Db.setPackageParams(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)

        assertTrue(TravelerValidator.isValidForPackageBooking(mockTraveler))
    }

    @Test
    fun testIsEmptyWithName() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(TravelerValidator.isTravelerEmpty(mockTraveler))
    }

    @Test
    fun testIsEmptyWithPhone() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(TravelerValidator.isTravelerEmpty(mockTraveler))
    }


    @Test
    fun testIsEmptyWithBirthDate() {
        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockAdultTravelerWithBirthDate(adultBirthDate)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(TravelerValidator.isTravelerEmpty(mockTraveler))
    }

    @Test
    fun testIsEmpty() {
        assertTrue(TravelerValidator.isTravelerEmpty(Traveler()))
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
        Mockito.`when`(mockTraveler.getPassengerCategory(any<PackageSearchParams>())).thenReturn(PassengerCategory.ADULT)

        return mockTraveler
    }

    private fun getInstanceOfPackageSearchParams(checkIn: LocalDate, checkOut: LocalDate) : PackageSearchParams {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(12)
                .startDate(checkIn)
                .endDate(checkOut)
                .origin(SuggestionV4())
                .destination(SuggestionV4())
        .build() as PackageSearchParams
        return packageParams
    }
}