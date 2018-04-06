package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.trips.TripAction
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinModifyReservationViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinModifyReservationViewModelTest {
    private lateinit var context: Context
    private lateinit var sut: HotelItinModifyReservationViewModel
    private val changeUrl = "www.change.com"
    private val cancelUrl = "www.cancel.com"
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    private val cancelReservationSubscriber = TestObserver<Unit>()
    private val changeReservationSubscriber = TestObserver<Unit>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = HotelItinModifyReservationViewModel(context)
        sut.cancelReservationSubject.subscribe(cancelReservationSubscriber)
        sut.changeReservationSubject.subscribe(changeReservationSubscriber)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun hotelHasCancelandChangeLinksTest() {
        val data = ItinCardDataHotelBuilder().build()
        val hotelTrip = data.tripComponent as TripHotel
        val room = hotelTrip.rooms[0]
        room.roomCancelLink = cancelUrl
        room.roomChangeLink = changeUrl
        assertEquals("", sut.cancelUrl)
        assertEquals("", sut.changeUrl)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.roomChangeSubject.onNext(room)
        assertEquals(cancelUrl, sut.cancelUrl)
        assertEquals(changeUrl, sut.changeUrl)
        cancelReservationSubscriber.assertValue(Unit)
        changeReservationSubscriber.assertValue(Unit)
    }

    @Test
    fun hotelHasNoCancelLinkAndHasChangeMobileLinkTest() {
        val data = ItinCardDataHotelBuilder().build()
        val hotelTrip = data.tripComponent as TripHotel
        val room = hotelTrip.rooms[0]
        room.roomCancelLink = ""
        room.roomChangeLinkForMobileWebView = changeUrl
        assertEquals("", sut.cancelUrl)
        assertEquals("", sut.changeUrl)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.roomChangeSubject.onNext(room)
        assertEquals("", sut.cancelUrl)
        assertEquals(changeUrl, sut.changeUrl)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertValue(Unit)
    }

    @Test
    fun cancelButtonOmnitureTest() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.cancelTextViewClickSubject.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Cancel", mockAnalyticsProvider)
    }

    @Test
    fun changeButtonOmnitureTest() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.changeTextViewClickSubject.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Change", mockAnalyticsProvider)
    }

    @Test
    fun cancelLearnMoreOmnitureTest() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.cancelLearnMoreClickSubject.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Cancel.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun changeLearnMoreOmnitureTest() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.changeLearnMoreClickSubject.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Change.LearnMore", mockAnalyticsProvider)
    }

    @Test
    fun testNotChangeableIsCancelable() {
        val data = ItinCardDataHotelBuilder().build()
        val hotelTrip = data.tripComponent as TripHotel
        sut.cancelUrl = cancelUrl
        sut.changeUrl = changeUrl
        val action = TripAction()
        action.isChangeable = false
        action.isCancellable = true
        hotelTrip.action = action
        assertEquals(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text, sut.helpDialogRes)
        sut.itinCardSubject.onNext(data)
        assertEquals(R.string.itin_flight_modify_widget_change_reservation_dialog_text, sut.helpDialogRes)
    }
}
