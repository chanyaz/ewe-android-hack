package com.expedia.bookings.test.robolectric

import android.view.View
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
import io.reactivex.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelRoomRateViewTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse
    lateinit private var hotelRoomRateView: HotelRoomRateView

    @Before fun before() {
        RuntimeEnvironment.application.setTheme(R.style.Theme_Hotels_Default)
        hotelRoomRateView = HotelRoomRateView(RuntimeEnvironment.application)
    }

    @Test
    fun soldOutRoomAutoCollapses() {
        givenHotelOffersResponse()
        hotelRoomRateView.viewModel = HotelRoomRateViewModel(RuntimeEnvironment.application, hotelOffersResponse.hotelId, hotelOffersResponse.hotelRoomResponse.first(), "", 0, PublishSubject.create<Int>(), false, LineOfBusiness.HOTELS)

        assertEquals(View.VISIBLE, hotelRoomRateView.hotelRoomRateActionButton.viewRoomButton.visibility)
        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.bookButton.visibility)
        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.soldOutButton.visibility)

        hotelRoomRateView.viewModel.collapseRoomObservable.onNext(Unit)

        hotelRoomRateView.expandedMeasurementsDone = true
        hotelRoomRateView.viewModel.expandRoomObservable.onNext(Unit)

        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.viewRoomButton.visibility)
        assertEquals(View.VISIBLE, hotelRoomRateView.hotelRoomRateActionButton.bookButton.visibility)
        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.soldOutButton.visibility)

        //Check the effects of selectedRoomSoldOut signal
        hotelRoomRateView.viewModel.roomSoldOut.onNext(true)
        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.viewRoomButton.visibility)
        assertEquals(View.GONE, hotelRoomRateView.hotelRoomRateActionButton.bookButton.visibility)
        assertEquals(View.VISIBLE, hotelRoomRateView.hotelRoomRateActionButton.soldOutButton.visibility)
    }

    private fun givenHotelOffersResponse() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
    }
}
