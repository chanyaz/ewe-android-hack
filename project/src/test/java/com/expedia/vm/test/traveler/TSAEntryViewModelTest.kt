package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.traveler.TSAEntryViewModel
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
    val TEST_GENDER = Traveler.Gender.MALE
    lateinit var tsaVM: TSAEntryViewModel

    val context = RuntimeEnvironment.application

    @Test
    fun testDefaultDate() {
        tsaVM = TSAEntryViewModel(context, Traveler())

        assertEquals(EXPECTED_DEFAULT_DATE.dayOfMonth, tsaVM.defaultDateSubject.value.dayOfMonth)
        assertEquals(EXPECTED_DEFAULT_DATE.year, tsaVM.defaultDateSubject.value.year)
        assertEquals(EXPECTED_DEFAULT_DATE.monthOfYear, tsaVM.defaultDateSubject.value.monthOfYear)
    }

    @Test
    fun testBirthDatePrePopulated() {
        var traveler = Traveler()
        traveler.birthDate = TEST_BIRTH_DATE
        tsaVM = TSAEntryViewModel(context, traveler)

        val testSubscriber = TestSubscriber<LocalDate>(1)
        tsaVM.birthDateSubject.subscribe(testSubscriber)

        assertEquals(TEST_BIRTH_DATE, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testGenderPrePopulated() {
        var traveler = Traveler()
        traveler.gender = TEST_GENDER
        tsaVM = TSAEntryViewModel(context, traveler)

        val testSubscriber = TestSubscriber<Traveler.Gender>(1)
        tsaVM.genderSubject.subscribe(testSubscriber)

        assertEquals(TEST_GENDER, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }
}