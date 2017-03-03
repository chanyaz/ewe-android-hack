package com.expedia.bookings.fragment

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.FrameLayout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinItemListFragmentTests {

    lateinit var sut: ItinItemListFragment
    lateinit var activity: NewPhoneLaunchActivity

    @Test
    fun hideLoadingViewWhenTripUpdated() {
        createSystemUnderTest()
        startVisibleFragment(sut)

        sut.showDeepRefreshLoadingView(true)
        assertLoadingViewVisible()

        sut.onTripUpdated(Trip())
        assertLoadingViewVisible(visible = false)
    }

    @Test
    fun showLoadingViewWhenTripUpdated() {
        createSystemUnderTest()
        startVisibleFragment(sut)

        sut.showDeepRefreshLoadingView(true)
        assertLoadingViewVisible()

        sut.onTripUpdateFailed(Trip())
        assertLoadingViewVisible(visible = false)
    }

    private fun assertLoadingViewVisible(visible: Boolean = true) {
        assertEquals(getLoadingView().visibility, if (visible) View.VISIBLE else View.GONE)
    }

    private fun getLoadingView(): FrameLayout {
        return sut.view?.findViewById(R.id.deep_refresh_loading_layout) as FrameLayout
    }

    private fun createSystemUnderTest() {
        sut = ItinItemListFragment()
        activity = Robolectric.buildActivity(NewPhoneLaunchActivity::class.java).create().get()
    }
}
