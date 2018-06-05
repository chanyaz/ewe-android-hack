package com.expedia.bookings.itin.lx

import android.content.Intent
import com.expedia.bookings.itin.lx.details.LxItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxItinDetailsActivityTest {
    lateinit var sut: LxItinDetailsActivity

    @Before
    fun setup() {
        val itinId = "LX_ITIN_ID1"
        val intent = Intent()
        intent.putExtra("LX_ITIN_ID", itinId)
        val fileUtils = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        fileUtils.writeTripToFile(itinId, getJsonStringFromMock("api/trips/lx_trip_details_for_mocker.json", null))
        sut = Robolectric.buildActivity(LxItinDetailsActivity::class.java, intent).create().start().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.lifecycleObserver.finishSubject.onNext(Unit)
        assertTrue(shadow.isFinishing)
    }
}
