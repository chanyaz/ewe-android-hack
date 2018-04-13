package com.expedia.bookings.itin.triplist

import android.graphics.drawable.ColorDrawable
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        loadTripListFragment()
        assertNotNull(testFragment.view)

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        val upcomingTab = tabLayout.getTabAt(0)!!
        assertEquals("Upcoming", upcomingTab.text)
        val pastTab = tabLayout.getTabAt(1)!!
        assertEquals("Past", pastTab.text)
        val cancelledTab = tabLayout.getTabAt(2)!!
        assertEquals("Cancelled", cancelledTab.text)
    }

    @Test
    fun `toolbar and tabs background color - Brands colors ab test not bucketed`() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        loadTripListFragment()

        val controlToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        val controlBackGroundColor = (controlToolbar.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.launch_toolbar_background_color), controlBackGroundColor)

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        val tabLayoutColor = (tabLayout.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.launch_toolbar_background_color), tabLayoutColor)
    }

    @Test
    fun `toolbar and tabs background color - Brands colors ab test bucketed`() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        loadTripListFragment()

        val variantToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        val variantBackGroundColor = (variantToolbar.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.brand_primary), variantBackGroundColor)

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        val tabLayoutColor = (tabLayout.background as ColorDrawable).color
        assertEquals(ContextCompat.getColor(activity, R.color.brand_primary), tabLayoutColor)
    }

    @Test
    fun toolbarVisibilityBottomNavUnbucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBottomNavTabs)
        loadTripListFragment()

        val controlToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        assertEquals(View.GONE, controlToolbar.visibility)
    }

    @Test
    fun toolbarVisibilityBottomNavBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppBottomNavTabs)
        loadTripListFragment()

        val variantToolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        assertEquals(View.VISIBLE, variantToolbar.visibility)
    }

    @Test
    fun testTrackTripListVisit() {
        val mockTripsTracking = MockTripsTracking()
        testFragment.tripsTracking = mockTripsTracking
        loadTripListFragment()
        assertFalse(mockTripsTracking.trackTripListVisited)
        testFragment.trackTripListVisit()
        assertTrue(mockTripsTracking.trackTripListVisited)
    }

    private fun loadTripListFragment() {
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
    }
}
