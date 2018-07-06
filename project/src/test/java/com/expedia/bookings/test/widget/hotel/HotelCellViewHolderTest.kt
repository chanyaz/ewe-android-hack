package com.expedia.bookings.test.widget.hotel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
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
import com.expedia.bookings.data.user.User
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.util.HotelFavoritesCache
import com.expedia.bookings.hotel.widget.HotelCellViewHolder
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class HotelCellViewHolderTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: HotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var pref: SharedPreferences by Delegates.notNull()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.hotel_cell, null, false) as ViewGroup
        hotelViewHolder = HotelCellViewHolder(hotelCellView)
        pref = PreferenceManager.getDefaultSharedPreferences(getContext())
    }

    @Test
    fun testSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)
        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.hotelPriceTopAmenity.soldOutTextView.visibility)
        Assert.assertEquals(View.GONE, hotelViewHolder.hotelPriceTopAmenity.priceContainer.visibility)
        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_sold_out_hotel_gray), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNotNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test
    fun testNewSoldOutTreatment() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.HotelSoldOutOnHSRTreatment)
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, hotelViewHolder.hotelPriceTopAmenity.soldOutTextView.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.soldOutOverlay.visibility)

        Assert.assertEquals(View.GONE, hotelViewHolder.hotelPriceTopAmenity.priceContainer.visibility)
        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_sold_out_hotel_gray), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNotNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test
    fun testReverseSoldOut() {
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)
        givenHotelMobileExclusive(hotel)
        givenHotelTonightOnly(hotel)
        givenHotelWithFewRoomsLeft(hotel)

        hotel.isSoldOut = false

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals(View.GONE, hotelViewHolder.hotelPriceTopAmenity.soldOutTextView.visibility)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.urgencyMessageContainer.urgencyIconImageView.visibility)

        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_detail_star_color), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test
    fun testReverseNewSoldOutTreatment() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.HotelSoldOutOnHSRTreatment)
        val hotel = makeHotel()
        givenSoldOutHotel(hotel)

        hotel.isSoldOut = false

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, hotelViewHolder.hotelPriceTopAmenity.soldOutTextView.visibility)
        Assert.assertEquals(View.GONE, hotelViewHolder.soldOutOverlay.visibility)
        Assert.assertEquals(ContextCompat.getColor(getContext(), R.color.hotelsv2_detail_star_color), hotelViewHolder.hotelNameStarAmenityDistance.ratingBar.getStarColor())
        Assert.assertNull(hotelViewHolder.imageView.colorFilter)
    }

    @Test
    fun testUrgencyMeassageFewRoomsLeft() {
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

    @Test
    fun testUrgencyMessageTonightOnly() {
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

    @Test
    fun testNoUrgencyMessage() {
        val hotel = makeHotel()

        hotelViewHolder.bindHotelData(hotel)

        Assert.assertEquals(View.GONE, hotelViewHolder.urgencyMessageContainer.visibility)
        Assert.assertEquals("", hotelViewHolder.urgencyMessageContainer.urgencyMessageTextView.text)
    }

    @Test
    fun testEarnMessaging() {
        val hotel = makeHotel()
        givenHotelWithFreeCancellation(hotel)

        PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")

        hotelViewHolder.bindHotelData(hotel)
        Assert.assertEquals(View.VISIBLE, hotelViewHolder.hotelPriceTopAmenity.topAmenityTextView.visibility)
        if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
            Assert.assertEquals(View.VISIBLE, hotelViewHolder.earnMessagingText.visibility)
        } else {
            Assert.assertEquals(View.GONE, hotelViewHolder.earnMessagingText.visibility)
        }
    }

    @Test
    fun testFavoriteHotelIconVisibility() {
        val hotel = makeHotel()
        hotelViewHolder.bindHotelData(hotel)
        assertEquals(View.GONE, hotelViewHolder.favoriteTouchTarget.visibility)

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.HotelShortlist)
        loginUser()

        hotelViewHolder.bindHotelData(hotel)
        assertEquals(View.VISIBLE, hotelViewHolder.favoriteTouchTarget.visibility)
    }

    @Test
    fun testFavoriteHotelIcon() {
        clearFavoritesCache()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.HotelShortlist)
        loginUser()

        val hotel = makeHotel()
        hotelViewHolder.bindHotelData(hotel)
        var imageDrawable = Shadows.shadowOf(hotelViewHolder.favoriteIcon.drawable)
        Assert.assertEquals(R.drawable.ic_favorite_inactive, imageDrawable.createdFromResId)

        HotelFavoritesCache.saveFavoriteId(getContext(), hotel.hotelId)
        hotelViewHolder.bindHotelData(hotel)

        imageDrawable = Shadows.shadowOf(hotelViewHolder.favoriteIcon.drawable)
        Assert.assertEquals(R.drawable.ic_favorite_active, imageDrawable.createdFromResId)
    }

    @Test
    fun testOnClick() {
        val hotel = makeHotel()
        hotelViewHolder.bindHotelData(hotel)

        val testObserver = TestObserver<Int>()
        hotelViewHolder.hotelClickedSubject.subscribe(testObserver)

        hotelViewHolder.itemView.callOnClick()
        testObserver.assertValue(-1)
    }

    @Test
    fun testHandleHotelInFavoriteSubject() {
        val hotel = makeHotel()
        hotelViewHolder.bindHotelData(hotel)
        hotelViewHolder.favoriteIcon.setImageDrawable(getContext().getDrawable(R.drawable.favorites_empty_heart))

        hotelViewHolder.viewModel.hotelInFavoriteSubject.onNext(Unit)
        assertEquals(getContext().getDrawable(R.drawable.ic_favorite_active), hotelViewHolder.favoriteIcon.drawable)
    }

    @Test
    fun testHandleHotelNotInFavoriteSubject() {
        val hotel = makeHotel()
        hotelViewHolder.bindHotelData(hotel)
        hotelViewHolder.favoriteIcon.setImageDrawable(getContext().getDrawable(R.drawable.favorites_empty_heart))

        hotelViewHolder.viewModel.hotelNotInFavoriteSubject.onNext(Unit)
        assertEquals(getContext().getDrawable(R.drawable.ic_favorite_inactive), hotelViewHolder.favoriteIcon.drawable)
    }

    private fun loginUser() {
        val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
        val user = User()
        UserLoginTestUtil.setupUserAndMockLogin(user, userStateManager)
    }

    private fun clearFavoritesCache() {
        val cache = getContext().getSharedPreferences(HotelFavoritesCache.FAVORITES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = cache.edit()
        editor.clear()
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy hotel"
        hotel.lowRateInfo = HotelRate()
        hotel.distanceUnit = "Miles"
        hotel.lowRateInfo.currencyCode = "USD"
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
