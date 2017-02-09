package com.expedia.vm.test.robolectric

import android.app.Activity
import android.graphics.drawable.Drawable
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.itin.ItinSignInViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinSignInViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: ItinSignInViewModel

    val statusTextTestSubscriber = TestSubscriber<String>()
    val buttonTextTestSubscriber = TestSubscriber<String>()
    val contDescTestSubscriber = TestSubscriber<String>()
    val imageTestSubscriber = TestSubscriber<Drawable>()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = ItinSignInViewModel(activity)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun signInButtonText() {
        assertEquals("Sign in with Expedia", sut.getSignInText())
    }

    @Test
    fun noUpcomingTripsMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestSubscriber)
        sut.updateButtonTextSubject.subscribe(buttonTextTestSubscriber)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestSubscriber)
        sut.statusImageSubject.subscribe(imageTestSubscriber)

        sut.setState(ItinSignInViewModel.MessageState.NO_UPCOMING_TRIPS)

        statusTextTestSubscriber.assertValue("No upcoming trips.")
        buttonTextTestSubscriber.assertValue("Refresh your trips")
        contDescTestSubscriber.assertValue("Refresh your trips Button")
        imageTestSubscriber.assertValueCount(1)
    }

    @Test
    fun failureMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestSubscriber)
        sut.updateButtonTextSubject.subscribe(buttonTextTestSubscriber)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestSubscriber)
        sut.statusImageSubject.subscribe(imageTestSubscriber)

        sut.setState(ItinSignInViewModel.MessageState.FAILURE)

        statusTextTestSubscriber.assertValue("Unable to connect at this time.")
        buttonTextTestSubscriber.assertValue("Refresh your trips")
        contDescTestSubscriber.assertValue("Refresh your trips Button")
        imageTestSubscriber.assertValueCount(1)
    }

    @Test
    fun tripsErrorMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestSubscriber)
        sut.updateButtonTextSubject.subscribe(buttonTextTestSubscriber)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestSubscriber)
        sut.statusImageSubject.subscribe(imageTestSubscriber)

        sut.setState(ItinSignInViewModel.MessageState.TRIPS_ERROR)

        statusTextTestSubscriber.assertValue("Unable to connect at this time.")
        buttonTextTestSubscriber.assertValue("Refresh your trips")
        contDescTestSubscriber.assertValue("Refresh your trips Button")
        imageTestSubscriber.assertValueCount(1)
    }

    @Test
    fun notLoggedInMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestSubscriber)
        sut.updateButtonTextSubject.subscribe(buttonTextTestSubscriber)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestSubscriber)
        sut.statusImageSubject.subscribe(imageTestSubscriber)

        sut.setState(ItinSignInViewModel.MessageState.NOT_LOGGED_IN)

        statusTextTestSubscriber.assertValue("Sign in to access your upcoming trips")
        buttonTextTestSubscriber.assertValue("Sign in with Expedia")
        contDescTestSubscriber.assertValue("Sign in with Expedia Button")
        imageTestSubscriber.assertValueCount(0)
    }

}
