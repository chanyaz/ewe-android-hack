package com.expedia.bookings.test.phone.hotels

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.support.test.rule.ActivityTestRule
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import org.junit.Rule
import org.junit.Test

class HotelDeepLinkTest {

    @Rule @JvmField
    val activityRule = ActivityTestRule(DeepLinkRouterActivity::class.java)

    @Test
    @Throws(Throwable::class)
    fun testHotelSearchWithMoreInfantsThanAdults() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelSearch?checkInDate=2018-01-01&checkOutDate=2018-01-02&numAdults=2&childAges=1,0,0&location=Miami")
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        activityRule.launchActivity(intent)

        HotelResultsScreen.waitForResultsLoaded()
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_results)
    }

    @Test
    @Throws(Throwable::class)
    fun testHotelDeepLinkWithLocation() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelSearch?location=Miami")
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        activityRule.launchActivity(intent)

        HotelResultsScreen.waitForResultsLoaded()
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_results)
    }

    // Google has decided to modify the deep links that we provide them, this test verifies that we properly handle
    // links that have been "Google-ified"
    @Test
    @Throws(Throwable::class)
    fun testHotelDeepLinkWithLocationFromGoogle() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelsearch/?location=Miami")
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        activityRule.launchActivity(intent)

        HotelResultsScreen.waitForResultsLoaded()
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_results)
    }

    @Test
    @Throws(Throwable::class)
    fun testHotelDeepLinkWithHotelId() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelSearch?hotelId=happypath")
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        activityRule.launchActivity(intent)

        HotelInfoSiteScreen.waitForDetailsLoaded()
        EspressoUtils.assertViewIsDisplayed(R.id.hotel_detail)
    }
}
