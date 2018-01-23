package com.expedia.vm.test.robolectric

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.itin.ItinPageUsableTracking
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.itin.ItinSignInViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mockito
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinSignInViewModelTest {
    private lateinit var activity: Activity
    private lateinit var sut: ItinSignInViewModel
    private lateinit var testItinSignInViewModel: TestItinSignInViewModel
    private lateinit var mockItinPageUsablePerformanceModel: ItinPageUsableTracking
    private lateinit var notificationManager: NotificationManager

    val statusTextTestSubscriber = TestObserver<String>()
    val buttonTextTestSubscriber = TestObserver<String>()
    val contDescTestSubscriber = TestObserver<String>()
    val imageTestSubscriber = TestObserver<Drawable>()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTripComponents()
        sut = ItinSignInViewModel(activity)
        notificationManager = NotificationManager(activity)
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
        imageTestSubscriber.assertValueCount(0)
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
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

    @Test
    fun testItinLoginStartTimerPageUsableTracking() {
        setUpItinLogin()
        givenCustomerNotAuthenticated()

        Mockito.verify(mockItinPageUsablePerformanceModel, Mockito.never()).markSuccessfulStartTime(Matchers.anyLong())
        testItinSignInViewModel.signInClickSubject.onNext(Unit)

        Mockito.verify(mockItinPageUsablePerformanceModel, Mockito.times(1)).markSuccessfulStartTime(Matchers.anyLong())
    }

    private fun setUpItinLogin() {
        testItinSignInViewModel = TestItinSignInViewModel(activity)
        mockItinPageUsablePerformanceModel = Mockito.mock(ItinPageUsableTracking::class.java)
        testItinSignInViewModel.itinPageUsablePerformanceModel = mockItinPageUsablePerformanceModel
    }

    private fun givenCustomerNotAuthenticated() {
        try {
            UserStateManager(activity, UserLoginStateChangedModel(), notificationManager).signOut()
        } catch (e: Exception) {
            // note: sign out triggers a notification clean-up which accesses the local DB.
            // As the DB isn't setup for the test it blows. We're just catching this so the test can still run.
        }
    }

    class TestItinSignInViewModel(context: Context) : ItinSignInViewModel(context) {
        override fun doItinSignIn() {
            this.userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        }
    }
}
