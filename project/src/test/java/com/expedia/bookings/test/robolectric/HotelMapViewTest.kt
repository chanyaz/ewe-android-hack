package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.widget.HotelMapView
import com.expedia.vm.HotelMapViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelMapViewTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse

    private var hotelMapView: HotelMapView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var selectARoomTestSubscriber = TestSubscriber.create<Unit>()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        hotelMapView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_map_widget, null) as HotelMapView
        hotelMapView.viewmodel = HotelMapViewModel(RuntimeEnvironment.application, selectARoomTestSubscriber)
    }

    @Test fun testSelectARoomClicked() {
        hotelMapView.selectRoomContainer.performClick()
        selectARoomTestSubscriber.assertValue(Unit)
    }

    @Test fun testMapViewWhenStrikethroughPriceAndPriceAreSame() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.VISIBLE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomContainer.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomPrice.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomLabel.visibility)
        assertEquals(View.GONE, hotelMapView.selectRoomStrikethroughPrice.visibility)

        assertEquals("happypath", hotelMapView.toolBarTitle.text)
        assertEquals(4f, hotelMapView.toolBarRating.getRating())
        assertEquals("Select a Room", hotelMapView.selectRoomLabel.text)
        assertEquals("From $109", hotelMapView.selectRoomPrice.text.toString())
    }

    @Test fun testMapViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.VISIBLE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomContainer.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomLabel.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomPrice.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomStrikethroughPrice.visibility)

        assertEquals("air_attached_hotel", hotelMapView.toolBarTitle.text)
        assertEquals(4f, hotelMapView.toolBarRating.getRating())
        assertEquals("Select a Room", hotelMapView.selectRoomLabel.text)
        assertEquals("From $241", hotelMapView.selectRoomPrice.text.toString())
        assertEquals("$284", hotelMapView.selectRoomStrikethroughPrice.text.toString())
    }

    @Test fun testMapViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.GONE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomContainer.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomLabel.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomPrice.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectRoomStrikethroughPrice.visibility)

        assertEquals("zero_star_rating", hotelMapView.toolBarTitle.text)
        assertEquals(0f, hotelMapView.toolBarRating.getRating())
        assertEquals("Select a Room", hotelMapView.selectRoomLabel.text)
        assertEquals("From $241", hotelMapView.selectRoomPrice.text.toString())
        assertEquals("$284", hotelMapView.selectRoomStrikethroughPrice.text.toString())
    }

    private fun givenHotelOffersResponseWhenHotelStarRatingIsZero() {
        hotelOffersResponse = mockHotelServiceTestRule.getZeroStarRatingHotelOffersResponse()
    }

    private fun givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
    }

    private fun givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent() {
        hotelOffersResponse = mockHotelServiceTestRule.getAirAttachedHotelOffersResponse()
    }
}
