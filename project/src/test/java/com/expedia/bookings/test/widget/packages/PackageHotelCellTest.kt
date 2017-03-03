package com.expedia.bookings.test.widget.packages

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
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
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.packages.PackageHotelCellViewHolder
import com.expedia.vm.packages.PackageHotelViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowResourcesEB::class))
class PackageHotelCellTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var packageHotelHolder: PackageHotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView, 200)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelFavoriteTest)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test fun testSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)
        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Sold Out", packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.GONE, packageHotelHolder.urgencyIcon.visibility)

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_sold_out_hotel_gray), packageHotelHolder.ratingBar.getStarColor())
        Assert.assertNotNull(packageHotelHolder.imageView.colorFilter)
    }

    @Test fun testReverseSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotel.isSoldOut = false

        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertNotEquals("Sold Out", packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyIcon.visibility)

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_detail_star_color), packageHotelHolder.ratingBar.getStarColor())
        Assert.assertNull(packageHotelHolder.imageView.colorFilter)
    }

    @Test fun testUrgencyMeassageFewRoomsLeft() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(activity.resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
                packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyIcon.visibility)
    }

    @Test fun testUrgencyMessageTonightOnly() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)

        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Tonight Only!", packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyIcon.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUrgencyMessageMobileExclusive() {
        val hotel = makeHotel()
        givenHotelMobileExclusive(hotel)

        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("Mobile Exclusive", packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.VISIBLE, packageHotelHolder.urgencyIcon.visibility)
    }

    @Test fun testNoUrgencyMessage() {
        val hotel = makeHotel()

        packageHotelHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, packageHotelHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("", packageHotelHolder.urgencyMessageBox.text)
        Assert.assertEquals(View.GONE, packageHotelHolder.urgencyIcon.visibility)
    }

    @Test fun testPriceIncludesFlights() {
        val hotel = makeHotel()
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertEquals(View.GONE, packageHotelHolder.priceIncludesFlightsView.visibility)

        hotel.isPackage = true
        hotel.thumbnailUrl = "https://media.expedia.com"
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertEquals(View.VISIBLE, packageHotelHolder.priceIncludesFlightsView.visibility)

    }

    @Test fun testNoPriceIncludesFlights() {
        val hotel = makeHotel()
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertEquals(View.GONE, packageHotelHolder.priceIncludesFlightsView.visibility)
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy"
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