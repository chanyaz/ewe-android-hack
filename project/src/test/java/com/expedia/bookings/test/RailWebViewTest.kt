package com.expedia.bookings.test

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.adobe.adms.measurement.ADMS_Measurement
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowApplication
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import com.expedia.bookings.R
import com.expedia.bookings.rail.activity.RailActivity
import com.expedia.ui.RailWebViewActivity

/**
 * Created by dkumarpanjabi on 7/6/17.
 */
@RunWith(RobolectricRunner::class)
class RailWebViewTest {

    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()
    private val APP_VISITOR_ID_PARAM = "appvi="
    private val RAILS_TAB_WEB_VIEW_URL = "https://www.expedia.de/bahn?mcicid=App.Rails.WebView";

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        Ui.getApplication(activity).defaultLaunchComponents()
        shadowApplication = ShadowApplication.getInstance()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun railsLaunchButtonOpensWebViewinDE() {
        setPOSWithRailWebViewEnabled(PointOfSaleId.GERMANY.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidRailHybridAppForDEEnabled)
        verifyRailsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun railsLaunchButtonOpensNativeinUK() {
        setPOSWithRailWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidRailHybridAppForDEEnabled)
        verifyRailsAppViewIsLaunched()
    }

    private fun goToRails() {
        NavUtils.goToRail(activity, null)
        activity.finish()
    }

    private fun setPOSWithRailWebViewEnabled(POSId: String) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, POSId)
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getRailUrlWithVisitorId(baseUrl: String): String {
        val visitorID = ADMS_Measurement.sharedInstance().visitorID
        return baseUrl + "&" + APP_VISITOR_ID_PARAM + visitorID
    }

    private fun verifyRailsWebViewIsLaunched() {
        goToRails()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(RailWebViewActivity::class.java.name, intent.component.className)
        assertEquals(getRailUrlWithVisitorId(RAILS_TAB_WEB_VIEW_URL), intentUrl)
    }

    private fun verifyRailsAppViewIsLaunched() {
        goToRails()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(RailActivity::class.java.name, intent.component.className)
    }
}