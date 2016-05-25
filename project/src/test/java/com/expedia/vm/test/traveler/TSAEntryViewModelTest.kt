package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TSAEntryViewModelTest {
    val EXPECTED_DEFAULT_DATE = LocalDate(1970, 1, 1)
    val TEST_BIRTH_DATE = LocalDate(1969, 10, 10)
    val TEST_GENDER = Traveler.Gender.GENDER
    val TEST_START_DATE = LocalDate.now()
    val TEST_END_DATE = LocalDate.now().plusDays(90)
    lateinit var tsaVM: TravelerTSAViewModel

    val context = RuntimeEnvironment.application

    val TEST_INFANT_ERROR = "This traveler must be under the age of 24 months for the entire trip to travel as an infant"
    val TEST_CHILD_ERROR = "This traveler must be under the age of 12 for the entire trip to travel as a child"
    val TEST_ADULT_CHILD_ERROR = "This traveler must be between 12 and 17 years old at the time of the trip"
    val TEST_ADULT_ERROR = "This traveler must be 18 years or older"

    @Test
    fun testDefaultDate() {
        tsaVM = TravelerTSAViewModel(context)
        tsaVM.updateTraveler(Traveler())

        assertEquals(EXPECTED_DEFAULT_DATE.dayOfMonth, tsaVM.defaultDateSubject.value.dayOfMonth)
        assertEquals(EXPECTED_DEFAULT_DATE.year, tsaVM.defaultDateSubject.value.year)
        assertEquals(EXPECTED_DEFAULT_DATE.monthOfYear, tsaVM.defaultDateSubject.value.monthOfYear)
    }

    @Test
    fun testBirthDatePrePopulated() {
        var traveler = Traveler()
        traveler.birthDate = TEST_BIRTH_DATE
        tsaVM = TravelerTSAViewModel(context)
        tsaVM.updateTraveler(traveler)

        val testSubscriber = TestSubscriber<LocalDate>(1)
        tsaVM.birthDateSubject.subscribe(testSubscriber)

        assertEquals(TEST_BIRTH_DATE, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testBirthDateErrors() {
        Db.setPackageParams(PackageSearchParams(SuggestionV4(), SuggestionV4(), TEST_START_DATE, TEST_END_DATE, 1, emptyList<Int>(), false))
        var traveler = Traveler()
        tsaVM = TravelerTSAViewModel(context)
        tsaVM.updateTraveler(traveler)

        val testSubscriber = TestSubscriber<String>()
        tsaVM.birthErrorTextSubject.subscribe(testSubscriber)

        traveler.searchedAge = 1
        traveler.setPassengerCategory(PassengerCategory.INFANT_IN_LAP)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))
        traveler.searchedAge = 5
        traveler.setPassengerCategory(PassengerCategory.CHILD)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(13))
        traveler.searchedAge = 12
        traveler.setPassengerCategory(PassengerCategory.ADULT_CHILD)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))
        traveler.searchedAge = -1
        traveler.setPassengerCategory(PassengerCategory.ADULT)
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(10))
        traveler.searchedAge = 18
        tsaVM.dateOfBirthObserver.onNext(LocalDate.now().minusYears(18))

        assertEquals(TEST_INFANT_ERROR, testSubscriber.onNextEvents[0])
        assertEquals(TEST_CHILD_ERROR, testSubscriber.onNextEvents[1])
        assertEquals(TEST_ADULT_CHILD_ERROR, testSubscriber.onNextEvents[2])
        assertEquals(TEST_ADULT_ERROR, testSubscriber.onNextEvents[3])
        testSubscriber.assertValueCount(4)
    }

    @Test
    fun testGenderPrePopulated() {
        var traveler = Traveler()
        traveler.gender = TEST_GENDER
        tsaVM = TravelerTSAViewModel(context)
        tsaVM.updateTraveler(traveler)

        val testSubscriber = TestSubscriber<Traveler.Gender>(1)
        tsaVM.genderSubject.subscribe(testSubscriber)

        assertEquals(TEST_GENDER, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }
}