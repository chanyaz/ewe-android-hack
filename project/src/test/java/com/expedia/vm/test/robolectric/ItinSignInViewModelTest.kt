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
    lateinit private var activity: Activity
    lateinit private var sut: ItinSignInViewModel
    lateinit private var testItinSignInViewModel: TestItinSignInViewModel
    lateinit private var mockItinPageUsablePerformanceModel: ItinPageUsableTracking
    lateinit private var notificationManager: NotificationManager

    val statusTextTestObserver = TestObserver<String>()
    val buttonTextTestObserver = TestObserver<String>()
    val contDescTestObserver = TestObserver<String>()
    val imageTestObserver = TestObserver<Drawable>()

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
        sut.statusTextSubject.subscribe(statusTextTestObserver)
        sut.updateButtonTextSubject.subscribe(buttonTextTestObserver)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestObserver)
        sut.statusImageSubject.subscribe(imageTestObserver)

        sut.setState(ItinSignInViewModel.MessageState.NO_UPCOMING_TRIPS)

        statusTextTestObserver.assertValue("No upcoming trips.")
        buttonTextTestObserver.assertValue("Refresh your trips")
        contDescTestObserver.assertValue("Refresh your trips Button")
        imageTestObserver.assertValueCount(0)
    }

    @Test
    fun failureMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestObserver)
        sut.updateButtonTextSubject.subscribe(buttonTextTestObserver)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestObserver)
        sut.statusImageSubject.subscribe(imageTestObserver)

        sut.setState(ItinSignInViewModel.MessageState.FAILURE)

        statusTextTestObserver.assertValue("Unable to connect at this time.")
        buttonTextTestObserver.assertValue("Refresh your trips")
        contDescTestObserver.assertValue("Refresh your trips Button")
        imageTestObserver.assertValueCount(1)
    }

    @Test
    fun tripsErrorMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestObserver)
        sut.updateButtonTextSubject.subscribe(buttonTextTestObserver)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestObserver)
        sut.statusImageSubject.subscribe(imageTestObserver)

        sut.setState(ItinSignInViewModel.MessageState.TRIPS_ERROR)

        statusTextTestObserver.assertValue("Unable to connect at this time.")
        buttonTextTestObserver.assertValue("Refresh your trips")
        contDescTestObserver.assertValue("Refresh your trips Button")
        imageTestObserver.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun notLoggedInMessageState() {
        sut.statusTextSubject.subscribe(statusTextTestObserver)
        sut.updateButtonTextSubject.subscribe(buttonTextTestObserver)
        sut.updateButtonContentDescriptionSubject.subscribe(contDescTestObserver)
        sut.statusImageSubject.subscribe(imageTestObserver)

        sut.setState(ItinSignInViewModel.MessageState.NOT_LOGGED_IN)

        statusTextTestObserver.assertValue("Sign in to access your upcoming trips")
        buttonTextTestObserver.assertValue("Sign in with Expedia")
        contDescTestObserver.assertValue("Sign in with Expedia Button")
        imageTestObserver.assertValueCount(0)
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
