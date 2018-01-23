package com.expedia.bookings.test.utils.validation

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.validation.TravelerValidator
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class TravelerValidatorTest {
    val mainTravelerIndex = 0
    val addTravelerIndex = 1
    val validFirstName = "Oscar"
    val validMiddleName = "T"
    val validLastName = "Grouch"
    val TEST_EMAIL = "test@gmail.com"
    val TEST_NUMBER = " 773 202 5862"

    val TOMORROW = LocalDate.now().plusDays(1)
    val TODAY = LocalDate.now()

    val travelerValidator: TravelerValidator by lazy { TravelerValidator(userStateManager) }
    val userStateManager: UserStateManager by lazy { UserLoginTestUtil.getUserStateManager() }

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
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(invalidBirthDate, PassengerCategory.ADULT)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun validInfantBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val validBirthDate = TOMORROW.plusDays(1).minusYears(2)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(validBirthDate, PassengerCategory.INFANT_IN_LAP)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidInfantBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val invalidBirthDate = TOMORROW.minusYears(2)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(invalidBirthDate, PassengerCategory.INFANT_IN_LAP)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateEmpty() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val mockTraveler = getMockTravelerWithBirthDateAndCategory(null, PassengerCategory.ADULT)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun invalidBirthDateToYoung() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val youngBirthDate = TOMORROW.minusYears(8)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.ADULT)

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be to young to be considered an adult, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun testValidBirthDateForAdultMinimumAge() {
        val youngBirthDate = TODAY.minusYears(18)
        val mockTraveler = givenTravelerWithCategoryAndBirthDate(youngBirthDate, PassengerCategory.ADULT)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    fun testInvalidBirthDateForYouthTooOld() {
        val youngBirthDate = TODAY.minusYears(18)
        val mockTraveler = givenTravelerWithCategoryAndBirthDate(youngBirthDate, PassengerCategory.ADULT_CHILD)

        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be too old to be considered a youth, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun testValidBirthDateForYouthMaximum() {
        val youngBirthDate = TODAY.minusYears(17)
        val mockTraveler = givenTravelerWithCategoryAndBirthDate(youngBirthDate, PassengerCategory.ADULT_CHILD)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun testValidBirthDateForYouthMinimum() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val youngBirthDate = TODAY.minusYears(12)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.ADULT_CHILD)
        travelerValidator.updateForNewSearch(packageSearchParams)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    fun testInvalidBirthDateForYouthTooYoung() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val youngBirthDate = TODAY.minusYears(11)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.ADULT_CHILD)
        travelerValidator.updateForNewSearch(packageSearchParams)

        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be too young to be considered a youth, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun testInvalidBirthDateForChildTooOld() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val youngBirthDate = TODAY.minusYears(12)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.CHILD)
        travelerValidator.updateForNewSearch(packageSearchParams)

        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be too old to be considered a child, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun testValidBirthDateForChildMaximumAge() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val youngBirthDate = TODAY.minusYears(11)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.CHILD)
        travelerValidator.updateForNewSearch(packageSearchParams)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun testValidBirthDateForChildLowRangeMinimumAge() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val youngBirthDate = TODAY.minusYears(2)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(youngBirthDate, PassengerCategory.CHILD)
        travelerValidator.updateForNewSearch(packageSearchParams)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler))
    }

    @Test
    fun testInvalidBirthDateTooYoungForChild() {
        val youngBirthDate = TOMORROW.minusYears(1)
        val mockTraveler = givenTravelerWithCategoryAndBirthDate(youngBirthDate, PassengerCategory.CHILD)

        assertFalse(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be too young to be considered a child, " +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange " +
                "if this tests fails look there as well")
    }

    @Test
    fun validBirthDate() {
        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = givenTravelerWithCategoryAndBirthDate(adultBirthDate, PassengerCategory.ADULT)

        assertTrue(travelerValidator.hasValidBirthDate(mockTraveler), "Traveler should be valid adult," +
                "unfortunately this test also depends on the static method PassengerCategory.isDateWithinPassengerCategoryRange" +
                "if this tests fails look there as well")
    }

    private fun givenTravelerWithCategoryAndBirthDate(birthDate: LocalDate, category: PassengerCategory): Traveler {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(birthDate, category)

        travelerValidator.updateForNewSearch(packageSearchParams)
        return mockTraveler
    }

    @Test
    fun testInvalidForPackageNoName() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoBirthDate() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        travelerValidator.updateForNewSearch(packageSearchParams)
        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoPhone() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testInvalidForPackageNoPassport() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val mockTraveler = givenTravelerOnlyMissingPassport()
        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, true))
    }

    @Test
    fun testValidMainForPackageBookingPassportNeeded() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val mockTraveler = givenTravelerOnlyMissingPassport()
        Mockito.`when`(mockTraveler.primaryPassportCountry).thenReturn("Mexico")

        assertTrue(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, true))
    }

    @Test
    fun testValidMainForPackageBooking() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))

        Mockito.`when`(mockTraveler.email).thenReturn(TEST_EMAIL)
        assertFalse(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))

        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        assertTrue(travelerValidator.isValidForFlightBooking(mockTraveler, mainTravelerIndex, false))
    }

    @Test
    fun testValidAddForPackageBooking() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())

        assertTrue(travelerValidator.isValidForFlightBooking(mockTraveler, addTravelerIndex, false))
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
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())

        assertFalse(travelerValidator.isTravelerEmpty(mockTraveler))
    }

    @Test
    fun testIsEmpty() {
        assertTrue(travelerValidator.isTravelerEmpty(Traveler()))
    }

    @Test
    fun testIsValidForRailBooking() {
        val mockTraveler = Mockito.mock(Traveler::class.java)

        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        assertFalse(travelerValidator.isValidForRailBooking(mockTraveler))

        Mockito.`when`(mockTraveler.email).thenReturn(TEST_EMAIL)
        assertFalse(travelerValidator.isValidForRailBooking(mockTraveler))

        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)
        assertTrue(travelerValidator.isValidForRailBooking(mockTraveler))
    }

    @Test
    fun testUserLoggedInMainTravelerValidation() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val user = UserLoginTestUtil.mockUser()
        val traveler = givenTravelerOnlyMissingPassport()
        user.primaryTraveler = traveler
        user.primaryTraveler.passengerCategory = PassengerCategory.ADULT

        UserLoginTestUtil.setupUserAndMockLogin(user, userStateManager)

        assertTrue(travelerValidator.isValidForFlightBooking(traveler, mainTravelerIndex, false))
    }

    @Test
    fun testUserLoggedInGuestValidation() {
        val packageSearchParams = getInstanceOfPackageSearchParams(LocalDate.now(), TOMORROW)
        travelerValidator.updateForNewSearch(packageSearchParams)

        val adultBirthDate = TOMORROW.minusYears(24)
        val guestTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)
        Mockito.`when`(guestTraveler.name).thenReturn(getValidName())

        val user = UserLoginTestUtil.mockUser()
        val traveler = givenTravelerOnlyMissingPassport()
        user.primaryTraveler = traveler
        user.primaryTraveler.passengerCategory = PassengerCategory.ADULT

        UserLoginTestUtil.setupUserAndMockLogin(user, userStateManager)

        assertTrue(travelerValidator.isValidForFlightBooking(guestTraveler, addTravelerIndex, false))
    }

    private fun getValidName(): TravelerName {
        val validName = TravelerName()
        validName.firstName = validFirstName
        validName.middleName = validMiddleName
        validName.lastName = validLastName

        return validName
    }

    private fun getMockTravelerWithBirthDateAndCategory(birthDate: LocalDate?, category: PassengerCategory): Traveler {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.birthDate).thenReturn(birthDate)
        Mockito.`when`(mockTraveler.passengerCategory).thenReturn(category)
        Mockito.`when`(mockTraveler.gender).thenReturn(Traveler.Gender.MALE)

        return mockTraveler
    }

    private fun getInstanceOfPackageSearchParams(checkIn: LocalDate, checkOut: LocalDate): PackageSearchParams {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(checkIn)
                .endDate(checkOut)
                .origin(SuggestionV4())
                .destination(SuggestionV4())
        .build() as PackageSearchParams
        return packageParams
    }

    private fun givenTravelerOnlyMissingPassport(): Traveler {
        val adultBirthDate = TOMORROW.minusYears(24)
        val mockTraveler = getMockTravelerWithBirthDateAndCategory(adultBirthDate, PassengerCategory.ADULT)

        Mockito.`when`(mockTraveler.name).thenReturn(getValidName())
        Mockito.`when`(mockTraveler.email).thenReturn(TEST_EMAIL)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(TEST_NUMBER)

        return mockTraveler
    }
}
