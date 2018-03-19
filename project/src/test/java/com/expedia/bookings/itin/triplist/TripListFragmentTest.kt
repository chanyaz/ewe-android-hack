package com.expedia.bookings.itin.triplist

import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class TripListFragmentTest {
    private lateinit var activity: AppCompatActivity
    private lateinit var fragmentManager: FragmentManager
    private val testFragment = TripListFragment()

    @Before
    fun setup() {
        RuntimeEnvironment.application.setTheme(R.style.ItinTheme)
        activity = Robolectric.setupActivity(AppCompatActivity::class.java)
        fragmentManager = activity.supportFragmentManager
    }

    @Test
    fun viewInflation() {
        assertNull(testFragment.view)
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        assertNotNull(testFragment.view)
    }

    @Test
    fun toolbarBackgroundBrandColorsUnbucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        val controlToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        val controlBackGroundColor = (controlToolbar.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.new_launch_toolbar_background_color), controlBackGroundColor)
    }

    @Test
    fun toolbarBackgroundBrandColorsBucketed() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        val variantToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        val variantBackGroundColor = (variantToolbar.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.brand_primary), variantBackGroundColor)
    }

    @Test
    fun toolbarVisibilityBottomNavUnbucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBottomNavTabs)
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        val controlToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        assertEquals(View.GONE, controlToolbar.visibility)
    }

    @Test
    fun toolbarVisibilityBottomNavBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppBottomNavTabs)
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        val variantToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        assertEquals(View.VISIBLE, variantToolbar.visibility)
    }
}
