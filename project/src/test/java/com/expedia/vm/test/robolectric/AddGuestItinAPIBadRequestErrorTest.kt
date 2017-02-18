package com.expedia.bookings.presenter.flight

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.MockModeShim
import com.expedia.bookings.utils.Ui
import com.expedia.vm.itin.AddGuestItinViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowResourcesEB::class))
class AddGuestItinAPIBadRequestErrorTest {
    private val context = RuntimeEnvironment.application

    lateinit private var sut: TestAddGuestItinViewModel

    @Before
    fun before() {
        Ui.getApplication(context).defaultTripComponents()
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        sut = TestAddGuestItinViewModel(context)
        sut.mockItineraryManager = mockItineraryManager

        MockModeShim.initMockWebServer(context)
        SettingUtils.save(context, context.getString(R.string.preference_which_api_to_use_key), "Mock Mode")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun badGuestItinRequestError() {
        val showErrorMessageSubscriber = TestSubscriber<String>()
        val showSearchDialogSubscriber = TestSubscriber<Unit>()

        sut.showItinFetchProgressObservable.subscribe(showSearchDialogSubscriber)
        sut.showErrorMessageObservable.subscribe(showErrorMessageSubscriber)

        sut.performGuestTripSearch.onNext(Pair("trip_error@mobiata.com", "error_bad_request_trip_response"))

        showSearchDialogSubscriber.requestMore(100L)
        showSearchDialogSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        showSearchDialogSubscriber.assertValueCount(1)

        showErrorMessageSubscriber.requestMore(100L)
        showErrorMessageSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        showErrorMessageSubscriber.assertValue("Unable to find itinerary. Please confirm on the Account screen that the Country setting matches the website address for your booking.")
    }

    class TestAddGuestItinViewModel(context: Context) : AddGuestItinViewModel(context) {
        lateinit var mockItineraryManager: ItineraryManager

        override fun getItinManager(): ItineraryManager {
            return mockItineraryManager
        }
    }
}