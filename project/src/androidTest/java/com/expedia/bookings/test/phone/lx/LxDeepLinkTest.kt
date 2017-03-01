package com.expedia.bookings.test.phone.lx

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.test.ActivityInstrumentationTestCase2
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.data.abacus.AbacusUtils.EBAndroidAppLXNavigateToSRP
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate

class LxDeepLinkTest : ActivityInstrumentationTestCase2<DeepLinkRouterActivity>(DeepLinkRouterActivity::class.java) {

    fun testDeeplinkForLXSearchControlState() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://activitySearch?location=San%20Francisco&startDate=" + DateUtils.localDateToyyyyMMdd(LocalDate.now()))
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        setActivityIntent(intent)
        activity

        LXScreen.didNotGoToResults()
        SearchScreen.selectDestinationTextView().check(matches(withText("San Francisco")))
    }

    fun testDeeplinkForLXSearchWithoutFilters() {
        AbacusTestUtils.bucketTests(EBAndroidAppLXNavigateToSRP)

        val intent = Intent()
        val deepLinkText = Uri.parse("expda://activitySearch?location=San%20Francisco&startDate=" + DateUtils.localDateToyyyyMMdd(LocalDate.now()))
        intent.data = deepLinkText
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
                "com.expedia.bookings.activity.DeepLinkRouterActivity")
        setActivityIntent(intent)
        activity

        LXScreen.didOpenResults()
        Common.delay(1)
        LXScreen.resultList().check(matches(LXScreen.withResults(4)))
    }

// 2017-03-01 -- removed for excessive flakiness
//    fun testDeeplinkForLXSearchWithFilters() {
//        AbacusTestUtils.bucketTests(EBAndroidAppLXNavigateToSRP)
//
//        val intent = Intent()
//        val deepLinkText = Uri.parse("expda://activitySearch?location=San%20Francisco&startDate=" + DateUtils.localDateToyyyyMMdd(LocalDate.now()) + "&filters=Private+Transfers|Shared+Transfers")
//        intent.data = deepLinkText
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.component = ComponentName(BuildConfig.APPLICATION_ID,
//                "com.expedia.bookings.activity.DeepLinkRouterActivity")
//        setActivityIntent(intent)
//        activity
//
//        LXScreen.didOpenResults()
//        Common.delay(1)
//        LXScreen.resultList().check(matches(LXScreen.withResults(1)))
//
//    }
}
