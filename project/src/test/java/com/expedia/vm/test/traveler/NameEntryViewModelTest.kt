package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.vm.traveler.NameEntryViewModel
import com.mobiata.android.validation.ValidationError
import org.junit.runner.RunWith;
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

import com.expedia.bookings.test.robolectric.RobolectricRunner;
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class NameEntryViewModelTest {
    lateinit var nameVM: NameEntryViewModel
    val TEST_FIRST = "Oscar"
    val TEST_MIDDLE = "The"
    val TEST_LAST = "Grouch"

    @Test
    fun emptyTraveler() {
        nameVM = NameEntryViewModel(Traveler())

        val testSubscriber = TestSubscriber<String>(1)
        nameVM.firstNameSubject.subscribe(testSubscriber)
        nameVM.middleNameSubject.subscribe(testSubscriber)
        nameVM.lastNameSubject.subscribe(testSubscriber)

        assertEquals("", testSubscriber.onNextEvents[0])
        assertEquals("", testSubscriber.onNextEvents[1])
        assertEquals("", testSubscriber.onNextEvents[2])
        testSubscriber.assertValueCount(3)
    }

    @Test
    fun travelerWithNames() {
        val traveler = Traveler()
        traveler.firstName = TEST_FIRST
        traveler.middleName = TEST_MIDDLE
        traveler.lastName = TEST_LAST

        nameVM = NameEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<String>(1)
        nameVM.firstNameSubject.subscribe(testSubscriber)
        nameVM.middleNameSubject.subscribe(testSubscriber)
        nameVM.lastNameSubject.subscribe(testSubscriber)

        assertEquals(TEST_FIRST, testSubscriber.onNextEvents[0])
        assertEquals(TEST_MIDDLE, testSubscriber.onNextEvents[1])
        assertEquals(TEST_LAST, testSubscriber.onNextEvents[2])
        testSubscriber.assertValueCount(3)
    }

    @Test
    fun travelerNameChange() {
        val traveler = Traveler()
        nameVM = NameEntryViewModel(traveler)

        nameVM.firstNameObserver.onNext(TEST_FIRST)
        assertEquals(TEST_FIRST, traveler.firstName)

        nameVM.middleNameObserver.onNext(TEST_MIDDLE)
        assertEquals(TEST_MIDDLE, traveler.middleName)

        nameVM.lastNameObserver.onNext(TEST_LAST)
        assertEquals(TEST_LAST, traveler.lastName)
    }

    @Test
    fun validateInvalidCharName() {
        nameVM = NameEntryViewModel(Traveler())

        val testSubscriber = TestSubscriber<Int>(1)
        nameVM.lastNameErrorSubject.subscribe(testSubscriber)
        var valid = nameVM.validateName("%@#$@!", nameVM.lastNameErrorSubject, true)
        assertFalse(valid)

        valid = nameVM.validateName("%@#$@!", nameVM.lastNameErrorSubject, false)
        assertFalse(valid)

        assertEquals(ValidationError.ERROR_DATA_INVALID, testSubscriber.onNextEvents[0])
        assertEquals(ValidationError.ERROR_DATA_INVALID, testSubscriber.onNextEvents[1])
        testSubscriber.assertValueCount(2)
    }

    @Test
    fun validateRequiredNameEmpty() {
        val traveler = Traveler()
        nameVM = NameEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<Int>(1)
        nameVM.firstNameErrorSubject.subscribe(testSubscriber)

        var valid = nameVM.validateName(null, nameVM.firstNameErrorSubject, true)
        assertFalse(valid)
        valid = nameVM.validateName("", nameVM.firstNameErrorSubject, true)
        assertFalse(valid)

        assertEquals(ValidationError.ERROR_DATA_MISSING, testSubscriber.onNextEvents[0])
        assertEquals(ValidationError.ERROR_DATA_MISSING, testSubscriber.onNextEvents[1])
        testSubscriber.assertValueCount(2)
    }

    @Test
    fun validateNotRequiredNameEmpty() {
        val traveler = Traveler()
        nameVM = NameEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<Int>(1)
        nameVM.middleNameErrorSubject.subscribe(testSubscriber)

        var valid = nameVM.validateName(null, nameVM.middleNameErrorSubject, false)
        assertTrue(valid)
        valid = nameVM.validateName("", nameVM.middleNameErrorSubject, false)
        assertTrue(valid)

        testSubscriber.assertNoValues()
    }

    @Test
    fun validateValidName() {
        val traveler = Traveler()
        nameVM = NameEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<Int>(1)
        nameVM.lastNameErrorSubject.subscribe(testSubscriber)

        var valid = nameVM.validateName(TEST_LAST, nameVM.lastNameErrorSubject, true)
        assertTrue(valid)
        testSubscriber.assertNoValues()
    }

    @Test
    fun validateAllNamesTriggersAllErrors() {
        val EXPECTED_ERROR_COUNT = 3
        var errorCount = 0
        var traveler = Traveler()
        traveler.middleName = "@!$%"
        nameVM = NameEntryViewModel(traveler)

        val testSubscriber = TestSubscriber<Int>(1)
        nameVM.firstNameErrorSubject.subscribe(testSubscriber)
        nameVM.middleNameErrorSubject.subscribe(testSubscriber)
        nameVM.lastNameErrorSubject.subscribe(testSubscriber)

        val valid = nameVM.validate()
        assertFalse(valid)
        // All 3 must trigger for proper error states
        testSubscriber.assertValueCount(3)
    }

    fun assertNamesEqual(expectedFirst: String, expectedMiddle: String, expectedLast: String) {
        nameVM.firstNameSubject.subscribe { name ->
            assertEquals(expectedFirst, name)
        }
        nameVM.middleNameSubject.subscribe { name ->
            assertEquals(expectedMiddle, name)
        }
        nameVM.lastNameSubject.subscribe { name ->
            assertEquals(expectedLast, name)
        }
    }
}
