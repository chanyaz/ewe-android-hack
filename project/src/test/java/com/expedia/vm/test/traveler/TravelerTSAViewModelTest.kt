package com.expedia.vm.test.traveler

import android.app.Activity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
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

    val testErrorTextSubscriber = TestSubscriber<String>()
    val testBirthDateErrorSubscriber = TestSubscriber<Boolean>()

    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testDefaultDate() {
        tsaVM = TravelerTSAViewModel(activity)
        tsaVM.updateTraveler(traveler)

        assertEquals(EXPECTED_DEFAULT_DATE.dayOfMonth, tsaVM.defaultDateSubject.value.dayOfMonth)
        assertEquals(EXPECTED_DEFAULT_DATE.year, tsaVM.defaultDateSubject.value.year)
        assertEquals(EXPECTED_DEFAULT_DATE.monthOfYear, tsaVM.defaultDateSubject.value.monthOfYear)
    }

    @Test
    fun testBirthDatePrePopulated() {
        traveler.birthDate = TEST_BIRTH_DATE
        tsaVM = TravelerTSAViewModel(activity)
        tsaVM.travelerValidator.updateForNewSearch(getTestParams())
        tsaVM.updateTraveler(traveler)

        val testSubscriber = TestSubscriber<LocalDate>(1)
        tsaVM.birthDateSubject.subscribe(testSubscriber)

        assertEquals(TEST_BIRTH_DATE, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testInfantBirthDateError() {
        setupDefaultTestModel()

        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(1, PassengerCategory.INFANT_IN_LAP)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))

        assertEquals(TEST_INFANT_ERROR, testErrorTextSubscriber.onNextEvents[0])
        assertTrue(testBirthDateErrorSubscriber.onNextEvents[0], "Expected Error State to be triggered")
    }

    @Test
    fun testChildBirthDateError() {
        setupDefaultTestModel()
        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(5, PassengerCategory.CHILD)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(13))

        assertEquals(TEST_CHILD_ERROR, testErrorTextSubscriber.onNextEvents[0])
        assertTrue(testBirthDateErrorSubscriber.onNextEvents[0], "Expected Error State to be triggered")
    }

    @Test
    fun testYouthBirthDateError() {
        setupDefaultTestModel()
        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(12, PassengerCategory.ADULT_CHILD)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))

        assertEquals(TEST_ADULT_CHILD_ERROR, testErrorTextSubscriber.onNextEvents[0])
        assertTrue(testBirthDateErrorSubscriber.onNextEvents[0], "Expected Error State to be triggered")
    }

    @Test
    fun testAdultBirthDateError() {
        setupDefaultTestModel()
        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(-1, PassengerCategory.ADULT)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))
        assertEquals(TEST_ADULT_ERROR, testErrorTextSubscriber.onNextEvents[0])
        assertTrue(testBirthDateErrorSubscriber.onNextEvents[0], "Expected Error State to be triggered")
    }

    @Test
    fun testValidBirthDateNoErrorText() {
        setupDefaultTestModel()
        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        setAgeEnteredAtSearch(18, PassengerCategory.ADULT)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(18))
        testErrorTextSubscriber.assertNoValues()
        testBirthDateErrorSubscriber.assertNoValues()
    }

    @Test
    fun testGenderPrePopulated() {
        traveler.gender = TEST_GENDER
        tsaVM = TravelerTSAViewModel(activity)
        tsaVM.updateTraveler(traveler)

        val testSubscriber = TestSubscriber<Traveler.Gender>(1)
        tsaVM.genderSubject.subscribe(testSubscriber)

        assertEquals(TEST_GENDER, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testUpdateTravelerInvalidPassengerCategory() {
        Db.setPackageParams(PackageSearchParams(SuggestionV4(), SuggestionV4(), TEST_START_DATE, TEST_END_DATE, 1, emptyList<Int>(), false))
        tsaVM = TravelerTSAViewModel(activity)
        tsaVM.travelerValidator.updateForNewSearch(getTestParams())
        setAgeEnteredAtSearch(1, PassengerCategory.INFANT_IN_SEAT)
        traveler.birthDate = LocalDate.now().minusYears(18)

        tsaVM.birthErrorTextSubject.subscribe(testErrorTextSubscriber)
        tsaVM.dateOfBirthErrorSubject.subscribe(testBirthDateErrorSubscriber)

        tsaVM.updateTraveler(traveler)
        assertEquals(TEST_INFANT_ERROR, testErrorTextSubscriber.onNextEvents[0])
        assertTrue(testBirthDateErrorSubscriber.onNextEvents[0], "Expected Error State to be triggered")
    }

    private fun setAgeEnteredAtSearch(searchedAge: Int, passengerCategory: PassengerCategory) {
        traveler.searchedAge = searchedAge
        traveler.setPassengerCategory(passengerCategory)
    }

    private fun setupDefaultTestModel() {
        val searchParams = getTestParams()
        Db.setPackageParams(searchParams)
        tsaVM = TravelerTSAViewModel(activity)
        tsaVM.travelerValidator.updateForNewSearch(searchParams)
        tsaVM.updateTraveler(traveler)
    }

    private fun getTestParams() : PackageSearchParams {
        return PackageSearchParams(SuggestionV4(), SuggestionV4(), TEST_START_DATE, TEST_END_DATE, 1, emptyList<Int>(), false)
    }
}