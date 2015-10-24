package com.expedia.bookings.test.phone.newhotels

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase

public class NewHotelDeepLinkTest: HotelTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal())
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testHotelDeepLinkWithLocation() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelSearch?location=Miami")
        intent.setData(deepLinkText)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setComponent(ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity"))
        Common.getApplication().startActivity(intent)

        Common.delay(3)
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_results)
    }

    @Throws(Throwable::class)
    fun testHotelDeepLinkWithHotelId() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://hotelSearch?hotelId=happypath")
        intent.setData(deepLinkText)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setComponent(ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity"))
        Common.getApplication().startActivity(intent)

        Common.delay(3)
        HotelScreen.waitForDetailsDisplayed()
    }

}
