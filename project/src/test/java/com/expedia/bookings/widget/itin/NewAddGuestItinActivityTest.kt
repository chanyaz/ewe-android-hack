package com.expedia.bookings.widget.itin

import com.expedia.bookings.itin.common.NewAddGuestItinActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class NewAddGuestItinActivityTest {
    private lateinit var activity: NewAddGuestItinActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(NewAddGuestItinActivity::class.java).create().get()
    }

    @Test
    fun testAdapterOnSyncFinshedWhenCalledFromActivity() {
        var newAddGuestItinActivityMock = Shadows.shadowOf(activity)
        activity.isSyncCalledFromHere = true
        activity.syncListenerAdapter.onSyncFinished(null)
        assertTrue { (newAddGuestItinActivityMock.isFinishing) }
    }

    @Test
    fun testAdapterOnSyncFinshedWhenNotCalledFromActivity() {
        var newAddGuestItinActivityMock = Shadows.shadowOf(activity)
        activity.isSyncCalledFromHere = false
        activity.syncListenerAdapter.onSyncFinished(null)
        assertFalse { (newAddGuestItinActivityMock.isFinishing) }
    }
}
