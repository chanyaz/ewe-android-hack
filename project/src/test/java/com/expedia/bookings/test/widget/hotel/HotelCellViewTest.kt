package com.expedia.bookings.test.widget.hotel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.mobiata.android.util.SettingUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelCellViewTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: HotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var pref: SharedPreferences by Delegates.notNull()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.hotel_cell, null, false) as ViewGroup
        hotelViewHolder = HotelCellViewHolder(hotelCellView, 200)
        pref = PreferenceManager.getDefaultSharedPreferences(getContext())
    }

    @Test fun testSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)
        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Sold Out", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_sold_out_hotel_gray), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNotNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test fun testReverseSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotel.isSoldOut = false

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertNotEquals("Sold Out", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_detail_star_color), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test fun testUrgencyMeassageFewRoomsLeft() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(activity.resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
                               hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)
    }

    @Test fun testUrgencyMessageTonightOnly() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Tonight Only!", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUrgencyMessageMobileExclusive() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Mobile Exclusive", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)
    }

    @Test fun testNoUrgencyMessage() {
        val hotel = makeHotel()

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)
    }

    @Test fun testEarnMessaging() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)

        val hotel = makeHotel()
        givenHotelWithFreeCancellation(hotel)

        PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")

        hotelViewHolder.bindHotelData(hotel)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.hotelPriceTopAmenity.topAmenityTextView.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.earnMessagingText.visibility)
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy hotel"
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

    private fun givenHotelWithFreeCancellation(hotel: Hotel) {
        hotel.hasFreeCancellation = true
    }
}
