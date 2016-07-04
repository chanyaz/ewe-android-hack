package com.expedia.vm.test.traveler

import android.app.Activity
import android.text.Editable
import com.expedia.bookings.data.Phone
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.TravelerPhoneViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerPhoneViewModelTest {
    val TEST_CODE = 355
    val TEST_CODE_STRING = "355"
    val TEST_NAME = "Albania"
    val TEST_NUMBER = " 773 202 5862"
    val TEST_NUMBER_EDITABLE = Editable.Factory().newEditable(TEST_NUMBER)

    lateinit var phoneVm: TravelerPhoneViewModel
    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun countryCodeUpdate() {
        var phone = Phone()
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        phoneVm.countryCodeObserver.onNext(TEST_CODE)

        assertEquals(TEST_CODE_STRING, phone.countryCode)
    }

    @Test
    fun countryNameUpdate() {
        var phone = Phone()
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        phoneVm.countryNameObserver.onNext(TEST_NAME)
        assertEquals(TEST_NAME, phone.countryName)
    }

    @Test
    fun phoneNumberUpdated() {
        var phone = Phone()
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        phoneVm.phoneNumberObserver.onNext(TEST_NUMBER)
        assertEquals(TEST_NUMBER, phone.number)
    }

    @Test
    fun travelerWithPhone() {
        var phone = Phone()
        phone.number = TEST_NUMBER
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        val testSubscriber = TestSubscriber<String>(1)
        phoneVm.phoneNumberSubject.subscribe(testSubscriber)

        assertEquals(TEST_NUMBER, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun invalidPhone() {
        var phone = Phone()
        phone.number = "12"
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        val testSubscriber = TestSubscriber<Boolean>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertFalse(phoneVm.validate())
        assertEquals(true, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun emptyPhone() {
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(Phone())

        val testSubscriber = TestSubscriber<Boolean>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertFalse(phoneVm.validate())
        assertEquals(true, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun validPhone() {
        var phone = Phone()
        phone.number = TEST_NUMBER
        phoneVm = TravelerPhoneViewModel(activity)
        phoneVm.updatePhone(phone)

        val testSubscriber = TestSubscriber<Boolean>(1)
        phoneVm.phoneErrorSubject.subscribe(testSubscriber)

        assertTrue(phoneVm.validate())
        assertEquals(false, testSubscriber.onNextEvents[0])
        testSubscriber.assertValueCount(1)
    }
}
