package com.expedia.vm.test

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.RoomInfoFields
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelWebCheckoutViewViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelWebCheckoutViewViewModelTest {

    lateinit var hotelWebCheckoutViewViewModel: HotelWebCheckoutViewViewModel
    private fun getContext() = RuntimeEnvironment.application
    lateinit var activity: Activity

    var servicesRule = ServicesRule(HotelServices::class.java)
        @Rule get

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        hotelWebCheckoutViewViewModel = HotelWebCheckoutViewViewModel(getContext())
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelWebCheckoutViewViewModel.createTripViewModel = HotelCreateTripViewModel(servicesRule.services!!, null)

    }

    @Test
    fun testFunctionalityOfDoCreateTrip() {
        var hotelSearchParams = getDummyHotelSearchParams()
        var roomInfoFields = RoomInfoFields(2, listOf(10, 10, 10))
        var hotelOfferResponse = mockHotelServiceTestRule.getHappyOfferResponse()
        var offerResponse = hotelOfferResponse.hotelRoomResponse.first()

        var testSubscriber = TestSubscriber<HotelCreateTripParams>()
        hotelWebCheckoutViewViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
        hotelWebCheckoutViewViewModel.offerObservable.onNext(offerResponse)
        hotelWebCheckoutViewViewModel.createTripViewModel.tripParams.subscribe(testSubscriber)
        hotelWebCheckoutViewViewModel.doCreateTrip()

        assertEquals(offerResponse.productKey, testSubscriber.onNextEvents[0].productKey)
        assertFalse(testSubscriber.onNextEvents[0].qualityAirAttach)
        assertEquals(roomInfoFields.room, testSubscriber.onNextEvents[0].roomInfoFields.room)
    }

    private fun getDummyHotelSearchParams(): HotelSearchParams {
        return HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                .destination(getDummySuggestion())
                .adults(2)
                .children(listOf(10, 10, 10))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }
}