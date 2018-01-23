package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.itin.AddGuestItinViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AddGuestItinViewModelTest {
    private lateinit var activity: Activity
    private lateinit var sut: AddGuestItinViewModel

    val hasEmailErrorSubscriber = TestObserver<Boolean>()
    val hasItinErrorSubscriber = TestObserver<Boolean>()

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
        assertTrue(hasEmailErrorSubscriber.values()[0])
        sut.itinNumberValidateObservable.onNext("12345678")
        assertTrue(hasItinErrorSubscriber.values()[0])

        sut.emailValidateObservable.onNext("testing@expedia.com")
        assertFalse(hasEmailErrorSubscriber.values()[1])
        sut.itinNumberValidateObservable.onNext("12345678")
        assertTrue(hasItinErrorSubscriber.values()[1])

        sut.emailValidateObservable.onNext("testing@expedia.com")
        assertFalse(hasEmailErrorSubscriber.values()[2])
        sut.itinNumberValidateObservable.onNext("12345678910")
        assertFalse(hasItinErrorSubscriber.values()[2])
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
