package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.HotelViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelCellViewTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: HotelListAdapter.HotelViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        hotelCellView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_cell, null, false) as ViewGroup
        hotelViewHolder = HotelListAdapter(PublishSubject.create<Hotel>(), PublishSubject.create<Unit>()).HotelViewHolder(hotelCellView, 200)
    }

    @Test fun testSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Sold Out", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyIcon.visibility)

        Assert.assertEquals(activity.getResources().getColor(R.color.hotelsv2_sold_out_hotel_gray), hotelViewHolder.ratingBar.getStarColor())
        Assert.assertNotNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test fun testReverseSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotel.isSoldOut = false

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertNotEquals("Sold Out", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)

        Assert.assertEquals(activity.getResources().getColor(R.color.hotelsv2_detail_star_color), hotelViewHolder.ratingBar.getStarColor())
        Assert.assertNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test fun testUrgencyMeassageFewRoomsLeft() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(activity.getResources().getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
                               hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testUrgencyMessageTonightOnly() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Tonight Only!", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testUrgencyMessageMobileExclusive() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Mobile Exclusive", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testNoUrgencyMessage() {
        val hotel = makeHotel()

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testPriceIncludesFlights() {
        val hotel = makeHotel()
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.priceIncludesFlightsView.visibility)

        hotel.isPackage = true
        hotel.thumbnailUrl = "https://media.expedia.com"
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.priceIncludesFlightsView.visibility)

    }

    @Test fun testNoPriceIncludesFlights() {
        val hotel = makeHotel()
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.priceIncludesFlightsView.visibility)
    }

    @Test fun testEarnMessaging() {
        val hotel = makeHotel()
        PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels = true
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.topAmenityTitle.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.earnMessagingText.visibility)
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.lowRateInfo = HotelRate()
        hotel.distanceUnit = "Miles"
        hotel.lowRateInfo.currencyCode = "USD"
        hotel.percentRecommended = 2
        hotel.lowRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 0, 320), null), false)
        return hotel
    }

    private fun givenSoldOutHotel(hotel: Hotel) {
        hotel.isSoldOut = true
    }

    private fun givenHotelTonightOnly(hotel: Hotel) {
        hotel.isSameDayDRR = true
    }

    private fun givenHotelMobileExclusive(hotel: Hotel) {
        hotel.isDiscountRestrictedToCurrentSourceType = true
    }

    private fun givenHotelWithFewRoomsLeft(hotel: Hotel) {
        hotel.roomsLeftAtThisRate = 3
    }
}