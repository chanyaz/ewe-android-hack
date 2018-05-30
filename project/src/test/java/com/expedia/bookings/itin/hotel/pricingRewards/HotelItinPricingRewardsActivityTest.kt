package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Intent
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockTripsTracking
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinPricingRewardsActivityTest {
    private lateinit var activity: HotelItinPricingRewardsActivity

    @Before
    fun setup() {
        val itinId = "ITIN1"
        val fileUtils = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        fileUtils.writeTripToFile(itinId, getJsonStringFromMock("api/trips/hotel_trip_details_for_mocker.json", null))
        val intent = Intent()
        intent.putExtra("ITINID", itinId)
        activity = Robolectric.buildActivity(HotelItinPricingRewardsActivity::class.java, intent).create().start().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        shadow.overridePendingTransition(-1, -1)
        activity.finish()
        assertNotEquals(-1, shadow.pendingTransitionEnterAnimationResourceId)
        assertNotEquals(-1, shadow.pendingTransitionExitAnimationResourceId)
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun testPricingPageload() {
        val mockTripsTracking = MockTripsTracking()
        activity.tripsTracking = mockTripsTracking
        activity.setUpOmnitureValues()

        activity.hotelRepo.liveDataItin.postValue(ItinMocker.hotelDetailsHappy)
        assertTrue(mockTripsTracking.trackHotelItinPricingRewardsPageload)

        mockTripsTracking.trackHotelItinPricingRewardsPageload = false
        activity.hotelRepo.liveDataItin.postValue(ItinMocker.hotelDetailsHappy)
        assertFalse(mockTripsTracking.trackHotelItinPricingRewardsPageload)
    }
}
