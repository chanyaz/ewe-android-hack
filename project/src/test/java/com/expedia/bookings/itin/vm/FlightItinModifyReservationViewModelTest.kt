package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripAction
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinModifyReservationViewModelTest {
    private lateinit var context: Context
    private lateinit var sut: FlightItinModifyReservationViewModel
    private val changeUrl = "www.change.com"
    private val cancelUrl = "www.cancel.com"

    private val cancelReservationSubscriber = TestObserver<Unit>()
    private val changeReservationSubscriber = TestObserver<Unit>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = FlightItinModifyReservationViewModel(context)
        sut.cancelReservationSubject.subscribe(cancelReservationSubscriber)
        sut.changeReservationSubject.subscribe(changeReservationSubscriber)
    }

    @Test
    fun flightIsChangeableAndCancelAbleTest() {
        val data = ItinCardDataFlightBuilder().build()
        val flightTrip = (data.tripComponent as TripFlight).flightTrip
        flightTrip.webChangePathURL = changeUrl
        flightTrip.webCancelPathURL = cancelUrl
        flightTrip.action = TripAction()
        flightTrip.action.isCancellable = true
        flightTrip.action.isChangeable = true
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        cancelReservationSubscriber.assertValue(Unit)
        changeReservationSubscriber.assertValue(Unit)
    }

    @Test
    fun flightIsNotChangeableTest() {
        val data = ItinCardDataFlightBuilder().build()
        val flightTrip = (data.tripComponent as TripFlight).flightTrip
        flightTrip.webChangePathURL = changeUrl
        flightTrip.webCancelPathURL = cancelUrl
        flightTrip.action = TripAction()
        flightTrip.action.isCancellable = true
        flightTrip.action.isChangeable = false
        changeReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        changeReservationSubscriber.assertNoValues()

        flightTrip.action.isChangeable = true
        flightTrip.webChangePathURL = ""
        changeReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        changeReservationSubscriber.assertNoValues()
        assertEquals(R.string.itin_flight_modify_widget_change_reservation_dialog_text, sut.helpDialogRes)
    }

    @Test
    fun flightIsNotCancelableTest() {
        val data = ItinCardDataFlightBuilder().build()
        val flightTrip = (data.tripComponent as TripFlight).flightTrip
        flightTrip.webChangePathURL = changeUrl
        flightTrip.webCancelPathURL = cancelUrl
        flightTrip.action = TripAction()
        flightTrip.action.isCancellable = false
        flightTrip.action.isChangeable = true
        cancelReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        cancelReservationSubscriber.assertNoValues()

        flightTrip.action.isCancellable = true
        flightTrip.webCancelPathURL = ""
        cancelReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        cancelReservationSubscriber.assertNoValues()

        assertEquals(R.string.itin_flight_modify_widget_cancel_reservation_dialog_text, sut.helpDialogRes)
    }

    @Test
    fun flightisNotCancelableNorChangeableTest() {
        val data = ItinCardDataFlightBuilder().build()
        val flightTrip = (data.tripComponent as TripFlight).flightTrip
        flightTrip.webChangePathURL = changeUrl
        flightTrip.webCancelPathURL = cancelUrl
        flightTrip.action = TripAction()
        flightTrip.action.isCancellable = false
        flightTrip.action.isChangeable = false
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.itinCardSubject.onNext(data)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        assertEquals(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text, sut.helpDialogRes)
    }

    @Test
    fun webViewIntentTest() {
        sut.changeUrl = changeUrl
        sut.webViewIntentSubject.subscribe {
            assertEquals(context.getString(R.string.itin_flight_modify_widget_change_reservation_text), it.getStringExtra("ARG_TITLE"))
            assertTrue(it.hasExtra("ARG_INJECT_EXPEDIA_COOKIES"))
            assertTrue(it.hasExtra("ARG_URL"))
            assertTrue(it.hasExtra("ARG_ALLOW_MOBILE_REDIRECTS"))
        }
        sut.changeTextViewClickSubject.onNext(Unit)
    }
}
