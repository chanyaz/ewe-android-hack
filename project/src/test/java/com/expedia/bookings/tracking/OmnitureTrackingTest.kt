package com.expedia.bookings.tracking

import com.adobe.adms.measurement.ADMS_Measurement
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DebugInfoUtils
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class OmnitureTrackingTest {

    @Test
    fun guidSentInProp23() {
        val context = RuntimeEnvironment.application
        val adms = ADMS_Measurement.sharedInstance(context)

        OmnitureTracking.trackAccountPageLoad()

        val expectedGuid = DebugInfoUtils.getMC1CookieStr(context).replace("GUID=", "")
        Assert.assertEquals(expectedGuid, adms.getProp(23))
    }

}