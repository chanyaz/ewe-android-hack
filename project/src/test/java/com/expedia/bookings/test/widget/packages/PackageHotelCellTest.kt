package com.expedia.bookings.test.widget.packages

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.packages.PackageHotelCellViewHolder
import com.expedia.vm.packages.PackageHotelViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackageHotelCellTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: PackageHotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        hotelViewHolder = PackageHotelCellViewHolder(hotelCellView, 200)
    }

    @Test fun testSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)
        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

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

        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

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

        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(activity.getResources().getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
                hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testUrgencyMessageTonightOnly() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)

        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Tonight Only!", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testUrgencyMessageMobileExclusive() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)

        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Mobile Exclusive", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testNoUrgencyMessage() {
        val hotel = makeHotel()

        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("", hotelViewHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyIcon.visibility)
    }

    @Test fun testPriceIncludesFlights() {
        val hotel = makeHotel()
        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.priceIncludesFlightsView.visibility)

        hotel.isPackage = true
        hotel.thumbnailUrl = "https://media.expedia.com"
        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.priceIncludesFlightsView.visibility)

    }

    @Test fun testNoPriceIncludesFlights() {
        val hotel = makeHotel()
        hotelViewHolder.bind(PackageHotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.priceIncludesFlightsView.visibility)
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