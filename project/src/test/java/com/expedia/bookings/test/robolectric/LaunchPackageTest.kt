package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.os.Bundle
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.NavigationHelper
import com.expedia.ui.PackageActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LaunchPackageTest {

    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO, MultiBrand.EBOOKERS, MultiBrand.LASTMINUTE, MultiBrand.WOTIF))
    fun testPackageLaunchIntent() {
        val data = Bundle()
        data.putBoolean(Constants.INTENT_PERFORM_HOTEL_SEARCH, true)

        NavigationHelper(activity).goToPackages(data, null)
        val intent = Shadows.shadowOf(activity).peekNextStartedActivity()
        assertEquals(PackageActivity::class.java.name, intent.component.className)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.MRJET, MultiBrand.VOYAGES))
    fun testPackageLaunchIntentWhenPackageIsNotEnabled() {
        val data = Bundle()
        data.putBoolean(Constants.INTENT_PERFORM_HOTEL_SEARCH, true)

        NavigationHelper(activity).goToPackages(data, null)
        val intent = Shadows.shadowOf(activity).peekNextStartedActivity()
        assertEquals(PhoneLaunchActivity::class.java.name, intent.component.className)
    }
}
