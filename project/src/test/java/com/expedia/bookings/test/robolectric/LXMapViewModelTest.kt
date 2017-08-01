package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.LXMapViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXMapViewModelTest {

    var mockActivityServiceTestRule: MockActivityServiceTestRule = MockActivityServiceTestRule()
        @Rule get

    lateinit private var activityOffersResponse: ActivityDetailsResponse
    private var lxMapViewModel: LXMapViewModel by notNullAndObservable {
        it.toolbarDetailText.subscribe(activityNameTestObserver)
        it.activityPrice.subscribe(activityPriceTestObserver)
        it.eventLatLng.subscribe(eventLatLngTestObserver)
        it.redemptionLocationsLatLng.subscribe(redemptionLocationsLatLngTestObserver)
    }

    val activityNameTestObserver = TestObserver<String>()
    val activityPriceTestObserver = TestObserver<CharSequence>()
    val eventLatLngTestObserver = TestObserver<LatLong>()
    val redemptionLocationsLatLngTestObserver = TestObserver<List<LatLong>>()

    @Before fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        lxMapViewModel = LXMapViewModel(activity)
        lxMapViewModel.lxState = LXState()
    }

    @Test fun testLXMapViewModelOutputs() {
        activityNameTestObserver.assertValueCount(0)
        eventLatLngTestObserver.assertValueCount(0)
        redemptionLocationsLatLngTestObserver.assertValueCount(0)
        activityPriceTestObserver.assertValueCount(0)

        givenActivityHappyDetailsResponse()
        lxMapViewModel.lxState.activity = LXActivity()
        lxMapViewModel.lxState.activity.title = activityOffersResponse.title
        lxMapViewModel.lxState.searchParams = LxSearchParams.Builder()
                .location("New York")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .build() as LxSearchParams;

        lxMapViewModel.offersObserver.onNext(activityOffersResponse)

        activityNameTestObserver.assertValueCount(1)
        eventLatLngTestObserver.assertValueCount(1)
        redemptionLocationsLatLngTestObserver.assertValueCount(1)
        activityPriceTestObserver.assertValueCount(1)

<<<<<<< HEAD
        activityNameTestSubscriber.assertValue("New York Pass: Visit up to 80 Attractions, Museums & Tours")
        compareLocationLatLng(eventLatLngTestSubscriber.onNextEvents[0]
                , ActivityDetailsResponse.LXLocation.getLocation(activityOffersResponse.eventLocation.latLng))
        redemptionLocationsLatLngTestSubscriber.assertValue(emptyList())
        assertEquals("From $130", activityPriceTestSubscriber.onNextEvents[0].toString())
=======
        activityNameTestObserver.assertValue("New York Pass: Visit up to 80 Attractions, Museums & Tours")
        compareLocationLatLng(eventLatLngTestObserver.onNextEvents.get(0)
                , ActivityDetailsResponse.LXLocation.getLocation(activityOffersResponse.eventLocation.latLng))
        redemptionLocationsLatLngTestObserver.assertValue(emptyList())
        assertEquals("From $130", activityPriceTestObserver.onNextEvents.get(0).toString())
>>>>>>> 5abc89409b... WIP

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