package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinModifyReservationViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinModifyReservationViewModelTest {
    private lateinit var context: Context
    private lateinit var sut: HotelItinModifyReservationViewModel

    private val cancelReservationSubscriber = TestObserver<Unit>()
    private val changeReservationSubscriber = TestObserver<Unit>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = HotelItinModifyReservationViewModel(context)
        sut.cancelReservationSubject.subscribe(cancelReservationSubscriber)
        sut.changeReservationSubject.subscribe(changeReservationSubscriber)
    }

    @Test
    fun cancelableAndChangeableTest() {
        sut.helpDialogRes = 0
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.setupCancelAndChange(true, true)
        cancelReservationSubscriber.assertValue(Unit)
        changeReservationSubscriber.assertValue(Unit)
        assertEquals(0, sut.helpDialogRes)
    }

    @Test
    fun cancelableAndNotChangeableTest() {
        sut.helpDialogRes = 0
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.setupCancelAndChange(true, false)
        cancelReservationSubscriber.assertValue(Unit)
        changeReservationSubscriber.assertNoValues()
        assertEquals(R.string.itin_flight_modify_widget_change_reservation_dialog_text, sut.helpDialogRes)
    }

    @Test
    fun notCancelableAndNotChangeableTest() {
        sut.helpDialogRes = 0
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.setupCancelAndChange(false, false)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        assertEquals(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text, sut.helpDialogRes)
    }

    @Test
    fun notCancelableAnChangeableTest() {
        sut.helpDialogRes = 0
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertNoValues()
        sut.setupCancelAndChange(false, true)
        cancelReservationSubscriber.assertNoValues()
        changeReservationSubscriber.assertValue(Unit)
        assertEquals(R.string.itin_flight_modify_widget_cancel_reservation_dialog_text, sut.helpDialogRes)
    }
}
