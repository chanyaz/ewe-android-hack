package com.expedia.bookings.launch.fragment

import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.launch.displaylogic.LaunchListStateManager
import com.expedia.bookings.launch.widget.LaunchListAdapter
import com.expedia.bookings.launch.widget.PhoneLaunchWidget
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PhoneLaunchFragmentTest {
    private val testFragment = PhoneLaunchFragment()

    lateinit var activity: AppCompatActivity
    lateinit var fragmentManager: FragmentManager
    lateinit var stateManager: LaunchListStateManager
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        RuntimeEnvironment.application.setTheme(R.style.LaunchTheme)
        activity = Robolectric.setupActivity(AppCompatActivity::class.java)
        stateManager = Ui.getApplication(activity).appComponent().launchListStateManager()
        fragmentManager = activity.supportFragmentManager
    }

    @Test
    fun recommendedHotels_areHidden_givenBucketedInHomeScreenDisplayLogic() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HomeScreenDisplayLogic)
        fragmentManager.beginTransaction().add(testFragment, "TAG").commitNow()
        val widget = testFragment.view!!.findViewById<PhoneLaunchWidget>(R.id.new_phone_launch_widget)
        val adapter = widget.launchListWidget.adapter as LaunchListAdapter
        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun testUpdatingLocationBeforeViewCreated() {
        assertNull(testFragment.view)
        testFragment.onReactToLocationRequest() // Should not crash. https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/7384

        fragmentManager.beginTransaction().add(testFragment, "TAG").commitNow()
        assertNotNull(testFragment.view)

        val testSubscriber = TestObserver.create<Unit>()
        val widget = testFragment.view!!.findViewById<PhoneLaunchWidget>(R.id.new_phone_launch_widget)
        widget.locationNotAvailable.subscribe(testSubscriber)
        testFragment.onReactToLocationRequest()

        assertEquals(1, testSubscriber.valueCount())
    }
}
