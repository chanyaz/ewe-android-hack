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
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.expedia.vm.hotel.HotelViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowResourcesEB::class))
class HotelCellViewFavoriteTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: HotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var pref: SharedPreferences by Delegates.notNull()
    private val hotelFavoriteChange = PublishSubject.create<Pair<String, Boolean>>()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before fun before() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelFavoriteTest)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.hotel_cell, null, false) as ViewGroup
        hotelViewHolder = HotelCellViewHolder(hotelCellView, 200, hotelFavoriteChange)
        pref = PreferenceManager.getDefaultSharedPreferences(getContext())
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

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_sold_out_hotel_gray), hotelViewHolder.ratingBar.getStarColor())
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

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_detail_star_color), hotelViewHolder.ratingBar.getStarColor())
        Assert.assertNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test fun testUrgencyMeassageFewRoomsLeft() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(activity.resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
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

    @Test fun testEarnMessaging() {
        val hotel = makeHotel()
        PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.GONE, hotelViewHolder.topAmenityTitle.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.earnMessagingText.visibility)
    }

    @Test fun testFavoriteButton() {
        val hotel = makeHotel()
        hotelViewHolder.bind(HotelViewModel(hotelViewHolder.itemView.context, hotel))
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.heartView.visibility)

        Assert.assertEquals(
                ResourcesCompat.getDrawable(hotelViewHolder.resources, R.drawable.favoriting_unselected_with_shadow, null),
                hotelViewHolder.heartView.drawable)

        // fav the hotel
        hotelViewHolder.heartView.callOnClick()
        Assert.assertEquals(
                ResourcesCompat.getDrawable(hotelViewHolder.resources, R.drawable.favoriting_selected_with_shadow, null),
                hotelViewHolder.heartView.drawable)

        // unfav the hotel
        hotelViewHolder.heartView.callOnClick()
        Assert.assertFalse(HotelFavoriteHelper.isHotelFavorite(getContext(), hotel.hotelId))
        Assert.assertEquals(
                ResourcesCompat.getDrawable(hotelViewHolder.resources, R.drawable.favoriting_unselected_with_shadow, null),
                hotelViewHolder.heartView.drawable)
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
}
