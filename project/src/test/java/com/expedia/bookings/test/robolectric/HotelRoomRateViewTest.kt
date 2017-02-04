package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.widget.HotelRoomRateView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelRoomRateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class HotelRoomRateViewTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse
    lateinit private var hotelRoomRateView: HotelRoomRateView

    @Before fun before() {
        RuntimeEnvironment.application.setTheme(R.style.Theme_Hotels_Control)
        hotelRoomRateView = HotelRoomRateView(RuntimeEnvironment.application)
    }

    @Test
    fun soldOutRoomAutoCollapses() {
        givenHotelOffersResponse()
        hotelRoomRateView.viewModel = HotelRoomRateViewModel(RuntimeEnvironment.application, hotelOffersResponse.hotelId, hotelOffersResponse.hotelRoomResponse.first(), "", 0, PublishSubject.create<Int>(), false, LineOfBusiness.HOTELS)

        assertEquals(true, hotelRoomRateView.viewRoom.isEnabled)
        assertEquals(false, hotelRoomRateView.viewRoom.isChecked)

        hotelRoomRateView.viewModel.collapseRoomObservable.onNext(Unit)

        hotelRoomRateView.expandedMeasurementsDone = true
        hotelRoomRateView.viewModel.expandRoomObservable.onNext(Unit)

        assertEquals(true, hotelRoomRateView.viewRoom.isEnabled)
        assertEquals(true, hotelRoomRateView.viewRoom.isChecked)

        //Check the effects of selectedRoomSoldOut signal
        hotelRoomRateView.viewModel.roomSoldOut.onNext(true)
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
