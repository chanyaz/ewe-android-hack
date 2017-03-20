package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.itin.AddGuestItinViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AddGuestItinViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: AddGuestItinViewModel

    val hasEmailErrorSubscriber = TestSubscriber<Boolean>()
    val hasItinErrorSubscriber = TestSubscriber<Boolean>()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTripComponents()
        sut = AddGuestItinViewModel(activity)
    }

    @Test
    fun testGuestFormFieldsErrorStates() {
        sut.hasEmailErrorObservable.subscribe(hasEmailErrorSubscriber)
        sut.hasItinErrorObservable.subscribe(hasItinErrorSubscriber)

        sut.emailValidateObservable.onNext("testing")
        hasEmailErrorSubscriber.assertValue(true)
        assertTrue(hasEmailErrorSubscriber.onNextEvents[0])
        sut.itinNumberValidateObservable.onNext("12345678")
        assertTrue(hasItinErrorSubscriber.onNextEvents[0])

        sut.emailValidateObservable.onNext("testing@expedia.com")
        assertFalse(hasEmailErrorSubscriber.onNextEvents[1])
        sut.itinNumberValidateObservable.onNext("12345678")
        assertTrue(hasItinErrorSubscriber.onNextEvents[1])

        sut.emailValidateObservable.onNext("testing@expedia.com")
        assertFalse(hasEmailErrorSubscriber.onNextEvents[2])
        sut.itinNumberValidateObservable.onNext("12345678910")
        assertFalse(hasItinErrorSubscriber.onNextEvents[2])
    }

    @Test
    fun testEmailValidation() {
        assertFalse(sut.isEmailValid("asd@"))
        assertTrue(sut.isEmailValid("asd@gmail.com"))
    }

    @Test
    fun testItinNumberValidation() {
        assertFalse(sut.isItinNumberValid("12345678"))
        assertTrue(sut.isItinNumberValid("123456789"))
    }
}
