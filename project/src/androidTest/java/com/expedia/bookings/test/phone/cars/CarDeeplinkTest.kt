package com.expedia.bookings.test.phone.cars

import android.test.ActivityInstrumentationTestCase2
import com.expedia.bookings.activity.DeepLinkRouterActivity

class CarDeeplinkTest : ActivityInstrumentationTestCase2<DeepLinkRouterActivity>(DeepLinkRouterActivity::class.java) {

    // 29-Aug-2017 : Disabling car UI tests since car is now a webview
//    @Test
//    @Throws(Throwable::class)
//    fun testCarDeeplinkDropoffTimeBeforePickup() {
//        val intent = Intent()
//        val dropOffBeforePickup = Uri.parse("expda://carSearch?pickupLocationLat=32.71444&pickupLocationLng=-117.16237&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-25T09:00:00&originDescription=SFO-San Francisco International Airport")
//        intent.data = dropOffBeforePickup
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
//                "com.expedia.bookings.activity.DeepLinkRouterActivity")
//        setActivityIntent(intent)
//        activity
//
//        EspressoUtils.assertViewIsDisplayed(R.id.widget_car_params)
//    }
//
//    @Test
//    @Throws(Throwable::class)
//    fun testCarDeeplinkURLWithNoParams() {
//        val intent = Intent()
//        val emptyParams = Uri.parse("expda://carSearch")
//        intent.data = emptyParams
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
//                "com.expedia.bookings.activity.DeepLinkRouterActivity")
//        setActivityIntent(intent)
//        activity
//
//        EspressoUtils.assertViewIsDisplayed(R.id.widget_car_params)
//    }
}
