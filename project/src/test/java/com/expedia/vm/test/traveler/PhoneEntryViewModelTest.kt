package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.traveler.PhoneEntryViewModel
import com.mobiata.android.validation.ValidationError
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
public class PhoneEntryViewModelTest {
    val TEST_CODE = 355
    val TEST_CODE_STRING = "355"
    val TEST_NAME = "Albania"
    val TEST_NUMBER = " 773 202 5862"
    lateinit var phoneVm: PhoneEntryViewModel

    @Test
    fun countryCodeUpdate() {
        var traveler = Traveler()
        phoneVm = PhoneEntryViewModel(traveler)

        phoneVm.countryCodeObserver.onNext(TEST_CODE)

        assertEquals(TEST_CODE_STRING, traveler.phoneCountryCode)
    }

    @Test
    fun countryNameUpdate() {
        var traveler = Traveler()
        phoneVm = PhoneEntryViewModel(traveler)

        phoneVm.countryNameObserver.onNext(TEST_NAME)
        assertEquals(TEST_NAME, traveler.phoneCountryName)
    }

    @Test
    fun phoneNumberUpdated() {
        var traveler = Traveler()
        phoneVm = PhoneEntryViewModel(traveler)
        phoneVm.phoneNumberObserver.onNext(TEST_NUMBER)
        assertEquals(TEST_NUMBER, traveler.phoneNumber)
    }

    @Test
    fun travelerWithPhone() {
        var traveler = Traveler()
        traveler.phoneNumber = TEST_NUMBER
        phoneVm = PhoneEntryViewModel(traveler)

        val testSubscriber = TestSubscriber<Phone>(1)
        phoneVm.phoneSubject.subscribe(testSubscriber)

        assertEquals(TEST_NUMBER, testSubscriber.onNextEvents[0].number)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun invalidPhone() {
        var traveler = Traveler()
        traveler.phoneNumber = "12"
        phoneVm = PhoneEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<Int>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertFalse(phoneVm.validate())
        assertEquals(ValidationError.ERROR_DATA_INVALID, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun emptyPhone() {
        phoneVm = PhoneEntryViewModel(Traveler())
        val testSubscriber = TestSubscriber<Int>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertFalse(phoneVm.validate())
        assertEquals(ValidationError.ERROR_DATA_MISSING, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun validPhone() {
        var traveler = Traveler()
        traveler.phoneNumber = TEST_NUMBER
        phoneVm = PhoneEntryViewModel(traveler)
        val testSubscriber = TestSubscriber<Int>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertTrue(phoneVm.validate())
        testSubscriber.assertNoValues()
    }
}
