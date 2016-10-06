package com.expedia.bookings.test.widget.hotel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
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
import com.expedia.bookings.test.robolectric.RobolectricRunner
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
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowResourcesEB::class))
class HotelCellViewFavoriteTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelViewHolder: HotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var pref: SharedPreferences by Delegates.notNull()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before fun before() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelFavoriteTest)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.new_hotel_cell_fav, null, false) as ViewGroup
        hotelViewHolder = HotelCellViewHolder(hotelCellView, 200)
        pref = PreferenceManager.getDefaultSharedPreferences(getContext())
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
}
