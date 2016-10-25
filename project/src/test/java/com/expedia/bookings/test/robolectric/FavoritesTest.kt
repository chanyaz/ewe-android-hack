package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.preference.PreferenceManager
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FavoritesTest {

    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
       }

    @Test
    fun testIsUserBucketed() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelFavoriteTest, 1)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.edit().putBoolean(activity.resources.getString(R.string.preference_enable_hotel_favorite), true).apply()
        assertTrue(HotelFavoriteHelper.showHotelFavoriteTest(activity, true))
    }

    @Test
    fun testIsFirstTimeFavoriting() {
        clearSharedPrefs()

        assertTrue(HotelFavoriteHelper.isFirstTimeFavoriting(activity))
        HotelFavoriteHelper.toggleHotelFavoriteState(activity, "AAAA")
        assertFalse(HotelFavoriteHelper.isFirstTimeFavoriting(activity))
    }

    @Test
    fun testGetHotelFavorites() {
        clearSharedPrefs()

        HotelFavoriteHelper.toggleHotelFavoriteState(activity, "AAAA")
        val favorites = HotelFavoriteHelper.getHotelFavorites(activity)
        assertTrue(favorites.contains("AAAA"))
        assertFalse(favorites.contains("ABCD"))
        assertEquals(favorites.size, 1)
    }

    @Test
    fun testIsHotelFavorite() {
        clearSharedPrefs()
        HotelFavoriteHelper.toggleHotelFavoriteState(activity, "AAAA")

        assertTrue(HotelFavoriteHelper.isHotelFavorite(activity, "AAAA"))
        assertFalse(HotelFavoriteHelper.isHotelFavorite(activity, "ABCD"))
    }

    @Test
    fun testToggleHotelFavoriteState() {
        clearSharedPrefs()

        HotelFavoriteHelper.toggleHotelFavoriteState(activity, "BBBB")
        assertTrue(HotelFavoriteHelper.isHotelFavorite(activity, "BBBB"))

        HotelFavoriteHelper.toggleHotelFavoriteState(activity, "BBBB")
        assertFalse(HotelFavoriteHelper.isHotelFavorite(activity, "BBBB"))

    }

    private fun clearSharedPrefs() {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreference.edit().clear().apply()
    }

}
