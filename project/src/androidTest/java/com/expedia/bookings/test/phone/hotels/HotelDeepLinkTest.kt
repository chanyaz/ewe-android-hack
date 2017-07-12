package com.expedia.bookings.test.phone.hotels

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.test.ActivityInstrumentationTestCase2
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import org.junit.Test

class HotelDeepLinkTest : ActivityInstrumentationTestCase2<DeepLinkRouterActivity>(DeepLinkRouterActivity::class.java) {

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
        setActivityIntent(intent)
        activity

        HotelScreen.waitForResultsLoaded()
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
        setActivityIntent(intent)
        activity

        HotelScreen.waitForResultsLoaded()
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
        setActivityIntent(intent)
        activity

        HotelScreen.waitForResultsLoaded()
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
        setActivityIntent(intent)
        activity

        HotelScreen.waitForDetailsLoaded()
        EspressoUtils.assertViewIsDisplayed(R.id.hotel_detail)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        val a = activity
        if (a != null) {
            var currentWaitTime = 0
            a.finish()
            while (!a.isDestroyed) {
                Common.delay(1)
                if (currentWaitTime++ > 10) {
                    throw RuntimeException("The activity: " + a.localClassName + " could not be destroyed within 10 seconds.")
                }
            }
            activity = null
        }
        super.tearDown()
    }
}
