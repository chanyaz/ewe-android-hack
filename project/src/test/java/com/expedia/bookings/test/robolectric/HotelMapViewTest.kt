package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.widget.HotelMapView
import com.expedia.vm.HotelMapViewModel
import com.google.android.gms.maps.MapView
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelMapViewTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private lateinit var hotelOffersResponse: HotelOffersResponse

    private var hotelMapView: HotelMapView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var selectARoomTestSubscriber = TestObserver.create<Unit>()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        val viewGroup = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_map_widget, null) as FrameLayout
        hotelMapView = viewGroup.findViewById<View>(R.id.hotel_map_view) as HotelMapView
        val detailsMapView = viewGroup.findViewById<View>(R.id.details_map_view) as MapView
        val detailsStub = hotelMapView.findViewById<View>(R.id.stub_map) as FrameLayout
        viewGroup.removeView(detailsMapView)
        detailsStub.addView(detailsMapView)
        hotelMapView.mapView = detailsMapView
        hotelMapView.mapView.getMapAsync(hotelMapView)
        hotelMapView.viewmodel = HotelMapViewModel(RuntimeEnvironment.application, selectARoomTestSubscriber, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
    }

    @Test fun testSelectARoomClicked() {
        hotelMapView.selectARoomBar.performClick()
        selectARoomTestSubscriber.assertValue(Unit)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testMapViewWhenStrikethroughPriceAndPriceAreSame() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.VISIBLE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomPrice.visibility)
        assertEquals(View.GONE, hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.visibility)

        assertEquals("happypath", hotelMapView.toolBarTitle.text)
        assertEquals(4f, hotelMapView.toolBarRating.getRating())
        assertEquals("$109", hotelMapView.selectARoomBar.selectRoomPrice.text.toString())
        assertEquals("Select a Room From $109 button", hotelMapView.selectARoomBar.selectRoomContainer.contentDescription)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testMapViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.VISIBLE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomContainer.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomPrice.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.visibility)

        assertEquals("air_attached_hotel", hotelMapView.toolBarTitle.text)
        assertEquals(4f, hotelMapView.toolBarRating.getRating())
        assertEquals("$241", hotelMapView.selectARoomBar.selectRoomPrice.text.toString())
        assertEquals("$284", hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.text.toString())
        assertEquals("Select a Room From $241 button", hotelMapView.selectARoomBar.selectRoomContainer.contentDescription)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testMapViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.GONE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomContainer.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomPrice.visibility)
        assertEquals(View.VISIBLE, hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.visibility)

        assertEquals("zero_star_rating", hotelMapView.toolBarTitle.text)
        assertEquals(0f, hotelMapView.toolBarRating.getRating())
        assertEquals("$241", hotelMapView.selectARoomBar.selectRoomPrice.text.toString())
        assertEquals("$284", hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.text.toString())
    }

    @Test fun testMapViewWhenRoomOffersAreNotAvailable() {
        hotelMapView.viewmodel = HotelMapViewModel(RuntimeEnvironment.application, selectARoomTestSubscriber, BehaviorSubject.createDefault<Boolean>(true), LineOfBusiness.HOTELS)
        givenHotelOffersResponseWhenRoomOffersAreNotAvailable()
        hotelMapView.viewmodel.offersObserver.onNext(hotelOffersResponse)

        assertEquals(View.VISIBLE, hotelMapView.toolBarTitle.visibility)
        assertEquals(View.VISIBLE, hotelMapView.toolBarRating.visibility)
        assertEquals(View.VISIBLE, hotelMapView.mapView.visibility)
        assertEquals(View.GONE, hotelMapView.selectARoomBar.visibility)
        assertEquals(View.GONE, hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.visibility)

        assertEquals("room_offers_not_available", hotelMapView.toolBarTitle.text)
        assertEquals(4f, hotelMapView.toolBarRating.getRating())
        assertEquals("", hotelMapView.selectARoomBar.selectRoomPrice.text.toString())
        assertEquals("", hotelMapView.selectARoomBar.selectRoomStrikeThroughPrice.text.toString())
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

    private fun givenHotelOffersResponseWhenRoomOffersAreNotAvailable() {
        hotelOffersResponse = mockHotelServiceTestRule.getRoomOffersNotAvailableHotelOffersResponse()
    }
}
