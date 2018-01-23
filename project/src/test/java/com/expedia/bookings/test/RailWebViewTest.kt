package com.expedia.bookings.test

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.navigation.NavUtils
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
import com.expedia.ui.LOBWebViewActivity
import kotlin.test.assertTrue

/**
 * Created by dkumarpanjabi on 7/6/17.
 */
@RunWith(RobolectricRunner::class)
class RailWebViewTest {

    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()

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
        val intentUrl = verifyRailsWebViewIsLaunched()
        assertTrue(intentUrl.startsWith("https://www.expedia.de/bahn?mcicid=App.Rails.WebView"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun railsLaunchButtonOpensNativeinUK() {
        setPOSWithRailWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidRailHybridAppForDEEnabled)
        verifyRailsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun railsLaunchButtonOpensWebViewInUK() {
        setPOSWithRailWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidRailHybridAppForUKEnabled)
        val intentUrl = verifyRailsWebViewIsLaunched()
        assertTrue(intentUrl.startsWith("https://www.expedia.co.uk/trains?mcicid=App.Rails.WebView"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun railsLaunchButtonOpensWebViewInUKForEbookers() {
        setPOSWithRailWebViewEnabled(PointOfSaleId.EBOOKERS_UNITED_KINGDOM.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidRailHybridAppForUKEnabled)
        val intentUrl = verifyRailsWebViewIsLaunched()
        assertTrue(intentUrl.startsWith("https://www.ebookers.com/trains?mcicid=App.Rails.WebView"))
    }

    private fun goToRails() {
        NavUtils.goToRail(activity, null, 0)
        activity.finish()
    }

    private fun setPOSWithRailWebViewEnabled(POSId: String) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, POSId)
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun verifyRailsWebViewIsLaunched(): String {
        goToRails()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")

        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.contains(PointOfSale.getPointOfSale().getRailUrlInfix()))
        assertTrue(intentUrl.contains("&adobe_mc="))
        return intentUrl
    }

    private fun verifyRailsAppViewIsLaunched() {
        goToRails()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(RailActivity::class.java.name, intent.component.className)
    }
}
