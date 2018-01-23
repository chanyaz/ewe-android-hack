package com.expedia.vm.test.traveler

import android.app.Activity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerTSAViewModelTest {
    val EXPECTED_DEFAULT_DATE = LocalDate(1970, 1, 1)
    val TEST_BIRTH_DATE = LocalDate(1969, 10, 10)
    val TEST_GENDER = Traveler.Gender.GENDER
    val TEST_START_DATE = LocalDate.now()
    val TEST_END_DATE = LocalDate.now().plusDays(90)
    lateinit var tsaVM: TravelerTSAViewModel

    var traveler = Traveler()

    val TEST_INFANT_ERROR = "This traveler must be under the age of 24 months for the entire trip to travel as an infant"
    val TEST_CHILD_ERROR = "This traveler must be under the age of 12 for the entire trip to travel as a child"
    val TEST_ADULT_CHILD_ERROR = "This traveler must be between 12 and 17 years old at the time of the trip"
    val TEST_ADULT_ERROR = "This traveler must be 18 years or older"
    val TEST_ADULT_CHILD_ERROR_V2 = "This traveler must be between the ages of 12 and 17 for the entire trip to travel as a youth"
    val TEST_CHILD_ERROR_V2 = "This traveler must be between the ages of 2 and 11 for the entire trip to travel as a child"

    val testErrorTextSubscriber = TestObserver<String>()
    val testBirthDateErrorSubscriber = TestObserver<Boolean>()

    var activity: Activity by Delegates.notNull()
    var travelerValidator: TravelerValidator by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        travelerValidator = Ui.getApplication(activity).travelerComponent().travelerValidator()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
    }

    @Test
    fun testDefaultDate() {
        tsaVM = TravelerTSAViewModel(traveler, activity)

        assertEquals(EXPECTED_DEFAULT_DATE.dayOfMonth, tsaVM.dateOfBirthViewModel.defaultDateSubject.value.dayOfMonth)
        assertEquals(EXPECTED_DEFAULT_DATE.year, tsaVM.dateOfBirthViewModel.defaultDateSubject.value.year)
        assertEquals(EXPECTED_DEFAULT_DATE.monthOfYear, tsaVM.dateOfBirthViewModel.defaultDateSubject.value.monthOfYear)
    }

    @Test
    fun testUpdateTravelerGenderErrorReset() {
        tsaVM = TravelerTSAViewModel(traveler, activity)
        val testSubscriber = TestObserver<Boolean>()
        tsaVM.genderViewModel.errorSubject.subscribe(testSubscriber)
        tsaVM.genderViewModel.errorSubject.onNext(true)

        assertEquals(true, testSubscriber.values()[0])
    }

    @Test
    fun testBirthDatePrePopulated() {
        travelerValidator.updateForNewSearch(getTestParams())
        traveler.birthDate = TEST_BIRTH_DATE
        tsaVM = TravelerTSAViewModel(traveler, activity)

        val testSubscriber = TestObserver<LocalDate>()
        tsaVM.dateOfBirthViewModel.birthDateSubject.subscribe(testSubscriber)

        assertEquals(TEST_BIRTH_DATE, testSubscriber.values()[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testInfantBirthDateError() {
        setupDefaultTestModel()

        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(1, PassengerCategory.INFANT_IN_LAP)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))

        assertEquals(TEST_INFANT_ERROR, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testChildBirthDateError() {
        setupDefaultTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(5, PassengerCategory.CHILD)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(13))

        assertEquals(TEST_CHILD_ERROR, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testYouthBirthDateError() {
        setupDefaultTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(12, PassengerCategory.ADULT_CHILD)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))

        assertEquals(TEST_ADULT_CHILD_ERROR, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testAdultBirthDateError() {
        setupDefaultTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(-1, PassengerCategory.ADULT)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))
        assertEquals(TEST_ADULT_ERROR, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testValidBirthDateNoErrorText() {
        setupDefaultTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(18, PassengerCategory.ADULT)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(18))
        testErrorTextSubscriber.assertNoValues()
        testBirthDateErrorSubscriber.assertNoValues()
    }

    @Test
    fun testGenderPrePopulated() {
        traveler.gender = TEST_GENDER
        tsaVM = TravelerTSAViewModel(traveler, activity)

        val testSubscriber = TestObserver<Traveler.Gender>()
        tsaVM.genderViewModel.genderSubject.subscribe(testSubscriber)

        assertEquals(TEST_GENDER, testSubscriber.values()[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testUpdateTravelerInvalidPassengerCategory() {
        Db.setPackageParams(PackageSearchParams(SuggestionV4(), SuggestionV4(), TEST_START_DATE, TEST_END_DATE, 1, emptyList<Int>(), false))
        travelerValidator.updateForNewSearch(getTestParams())
        setAgeEnteredAtSearch(1, PassengerCategory.INFANT_IN_SEAT)
        traveler.birthDate = LocalDate.now().minusYears(18)
        tsaVM = TravelerTSAViewModel(traveler, activity)

        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)

        assertEquals(TEST_INFANT_ERROR, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testBucketedChildBirthDateError() {
        setupBucketedTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)
        setAgeEnteredAtSearch(6, PassengerCategory.CHILD)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(100))

        assertEquals(TEST_CHILD_ERROR_V2, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    @Test
    fun testBucketedYouthBirthDateError() {
        setupBucketedTestModel()
        tsaVM.dateOfBirthViewModel.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthViewModel.errorSubject.subscribe(testBirthDateErrorSubscriber)
        setAgeEnteredAtSearch(14, PassengerCategory.ADULT_CHILD)
        tsaVM.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate.now().minusYears(100))

        assertEquals(TEST_ADULT_CHILD_ERROR_V2, testErrorTextSubscriber.values()[0])
        assertTrue(testBirthDateErrorSubscriber.values()[0], "Expected Error State to be triggered")
    }

    private fun setAgeEnteredAtSearch(searchedAge: Int, passengerCategory: PassengerCategory) {
        traveler.searchedAge = searchedAge
        traveler.passengerCategory = passengerCategory
    }

    private fun setupDefaultTestModel() {
        setSearchParams()
        tsaVM = TravelerTSAViewModel(traveler, activity.applicationContext)
    }

    private fun setupBucketedTestModel() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        setSearchParams()
        tsaVM = TravelerTSAViewModel(traveler, activity.applicationContext)
     }

    private fun setSearchParams() {
        val searchParams = getTestParams()
        Db.setPackageParams(searchParams)
        travelerValidator.updateForNewSearch(searchParams)
    }

    private fun getTestParams(): PackageSearchParams {
        return PackageSearchParams(SuggestionV4(), SuggestionV4(), TEST_START_DATE, TEST_END_DATE, 1, emptyList<Int>(), false)
    }
}
