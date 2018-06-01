package com.expedia.bookings.lx.vm

import android.app.Activity
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.MockActivityObjects
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.util.notNullAndObservable
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXMapViewModelTest {

    private val mockActivityServiceTestRule = MockActivityObjects()

    private lateinit var activityOffersResponse: ActivityDetailsResponse
    private var lxMapViewModel: LXMapViewModel by notNullAndObservable {
        it.toolbarDetailText.subscribe(activityNameTestSubscriber)
        it.activityPrice.subscribe(activityPriceTestSubscriber)
        it.eventLatLng.subscribe(eventLatLngTestSubscriber)
        it.redemptionLocationsLatLng.subscribe(redemptionLocationsLatLngTestSubscriber)
    }

    val activityNameTestSubscriber = TestObserver<String>()
    val activityPriceTestSubscriber = TestObserver<CharSequence>()
    val eventLatLngTestSubscriber = TestObserver<LatLong>()
    val redemptionLocationsLatLngTestSubscriber = TestObserver<List<LatLong>>()

    @Before fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        lxMapViewModel = LXMapViewModel(activity)
        lxMapViewModel.lxState = LXState()
    }

    @Test fun testLXMapViewModelOutputs() {
        activityNameTestSubscriber.assertValueCount(0)
        eventLatLngTestSubscriber.assertValueCount(0)
        redemptionLocationsLatLngTestSubscriber.assertValueCount(0)
        activityPriceTestSubscriber.assertValueCount(0)

        givenActivityHappyDetailsResponse()
        lxMapViewModel.lxState.activity = LXActivity()
        lxMapViewModel.lxState.activity.title = activityOffersResponse.title
        lxMapViewModel.lxState.searchParams = LxSearchParams.Builder()
                .location("New York")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .build() as LxSearchParams

        lxMapViewModel.offersObserver.onNext(activityOffersResponse)

        activityNameTestSubscriber.assertValueCount(1)
        eventLatLngTestSubscriber.assertValueCount(1)
        redemptionLocationsLatLngTestSubscriber.assertValueCount(1)
        activityPriceTestSubscriber.assertValueCount(1)

        activityNameTestSubscriber.assertValue("New York Pass: Visit up to 80 Attractions, Museums & Tours")
        compareLocationLatLng(eventLatLngTestSubscriber.values()[0]
                , ActivityDetailsResponse.LXLocation.getLocation(activityOffersResponse.eventLocation.latLng))
        redemptionLocationsLatLngTestSubscriber.assertValue(emptyList())
        assertEquals("From $130", activityPriceTestSubscriber.values()[0].toString())
    }

    @Test fun testFromPriceStyledString() {
        givenActivityHappyDetailsResponse()
        assertEquals("From $130", LXMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, activityOffersResponse).toString())
    }

    private fun compareLocationLatLng(expectedLocation: LatLong, actualLocation: LatLong): Boolean {
        return expectedLocation.latitude.equals(actualLocation.latitude)
                && expectedLocation.longitude.equals(actualLocation.longitude)
    }

    private fun givenActivityHappyDetailsResponse() {
        activityOffersResponse = mockActivityServiceTestRule.getHappyOffersResponse()
    }
}
