package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = [(ShadowGCM::class), (ShadowUserManager::class), (ShadowAccountManagerEB::class)])
class FlightTravelerFrequentFlyerWidgetTooltipTest {

    private lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        Db.sharedInstance.clear()
        Db.sharedInstance.travelers.add(Traveler())
    }

    @Test
    fun testTooltipVisibilityOFFWhenABTestIsControl() {
        val widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_entry_widget, null) as FlightTravelerEntryWidget

        widget.viewModel = FlightTravelerEntryWidgetViewModel(activity, 0, BehaviorSubject.createDefault(false), TravelerCheckoutStatus.CLEAN)

        val frequentFlyerTooltip = widget.findViewById<TextView>(R.id.frequent_flyer_tooltip)
        assertEquals(View.GONE, frequentFlyerTooltip.visibility)
    }

    @Test
    fun testTooltipVisibilityONWhenABTestIsBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFrequentFlierTooltip)

        val widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_entry_widget, null) as FlightTravelerEntryWidget
        widget.viewModel = FlightTravelerEntryWidgetViewModel(activity, 0, BehaviorSubject.createDefault(false), TravelerCheckoutStatus.CLEAN)

        val frequentFlyerTooltip = widget.findViewById<TextView>(R.id.frequent_flyer_tooltip)
        assertEquals(View.VISIBLE, frequentFlyerTooltip.visibility)
    }
}
