package com.expedia.bookings.test.robolectric

import android.view.View
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.widget.HotelRoomRateView
import com.expedia.bookings.widget.ScrollView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelRoomRateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelRoomRateViewTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse
    lateinit private var hotelRoomRateView: HotelRoomRateView

    @Before fun before() {
        hotelRoomRateView = HotelRoomRateView(RuntimeEnvironment.application, ScrollView(RuntimeEnvironment.application), BehaviorSubject.create<View>(View(RuntimeEnvironment.application)), endlessObserver {  }, 0)
    }

    @Test
    fun soldOutRoomAutoCollapses() {
        givenHotelOffersResponse()

        hotelRoomRateView.viewmodel = HotelRoomRateViewModel(RuntimeEnvironment.application, hotelOffersResponse.hotelId, hotelOffersResponse.hotelRoomResponse.first(), "", 0, PublishSubject.create<Int>())

        assertEquals(true, hotelRoomRateView.viewRoom.isEnabled)
        assertEquals(false, hotelRoomRateView.viewRoom.isChecked)

        hotelRoomRateView.viewmodel.collapseRoomObservable.onNext(false)

        hotelRoomRateView.viewmodel.expandedMeasurementsDone.onNext(Unit)
        hotelRoomRateView.viewmodel.expandRoomObservable.onNext(false)

        assertEquals(true, hotelRoomRateView.viewRoom.isEnabled)
        assertEquals(true, hotelRoomRateView.viewRoom.isChecked)

        //Check the effects of selectedRoomSoldOut signal
        hotelRoomRateView.viewmodel.roomSoldOut.onNext(true)
        assertEquals(false, hotelRoomRateView.viewRoom.isEnabled)
        assertEquals(false, hotelRoomRateView.viewRoom.isChecked)
        assertEquals("Sold Out", hotelRoomRateView.viewRoom.text)
        assertEquals("Sold Out", hotelRoomRateView.viewRoom.textOn)
        assertEquals("Sold Out", hotelRoomRateView.viewRoom.textOff)
    }

    private fun givenHotelOffersResponse() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
    }
}
