package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.vm.itin.AddGuestItinViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class AddGuestItinAPIErrorTest {
    private val context = RuntimeEnvironment.application

    lateinit private var sut: TestAddGuestItinViewModel

    lateinit private var mockItineraryManager: ItineraryManager

    @Before
    fun before() {
        sut = TestAddGuestItinViewModel(context)
        mockItineraryManager = sut.mockItineraryManager
        sut.addItinSyncListener()

        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Mock Mode")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun notAuthenticatedGuestItinError() {
        val showErrorMessageSubscriber = TestObserver<String>()
        val showSearchDialogSubscriber = TestObserver<Unit>()

        val email = "trip_error@mobiata.com"
        val tripNumber = "error_trip_response"

        sut.showItinFetchProgressObservable.subscribe(showSearchDialogSubscriber)
        sut.showErrorMessageObservable.subscribe(showErrorMessageSubscriber)

        Mockito.doNothing().`when`(mockItineraryManager).addGuestTrip(email, tripNumber)

        sut.performGuestTripSearch.onNext(Pair(email, tripNumber))
        mockItineraryManager.onTripFailedFetchingRegisteredUserItinerary()

        showSearchDialogSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        showSearchDialogSubscriber.assertValueCount(1)

        showErrorMessageSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        showErrorMessageSubscriber.assertValue("This is not a guest itinerary. Please sign into the Expedia account associated with this itinerary.")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun badGuestItinRequestError() {
        val showErrorMessageSubscriber = TestObserver<String>()
        val showSearchDialogSubscriber = TestObserver<Unit>()

        val email = "trip_error@mobiata.com"
        val tripNumber = "error_bad_request_trip_response"

        sut.showItinFetchProgressObservable.subscribe(showSearchDialogSubscriber)
        sut.showErrorMessageObservable.subscribe(showErrorMessageSubscriber)

        Mockito.doNothing().`when`(mockItineraryManager).addGuestTrip(email, tripNumber)

        sut.performGuestTripSearch.onNext(Pair(email, tripNumber))
        mockItineraryManager.onTripFailedFetchingGuestItinerary()

        showSearchDialogSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        showSearchDialogSubscriber.assertValueCount(1)

        showErrorMessageSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        showErrorMessageSubscriber.assertValue("Unable to find itinerary. Please confirm on the Account screen that the Country setting matches the website address for your booking.")
    }

    class TestAddGuestItinViewModel(context: Context) : AddGuestItinViewModel(context) {
        var mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())

        override fun getItinManager(): ItineraryManager {
            return mockItineraryManager
        }
    }
}