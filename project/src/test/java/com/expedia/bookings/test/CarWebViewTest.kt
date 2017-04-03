package com.expedia.bookings.test


import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.adobe.adms.measurement.ADMS_Measurement
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.ui.CarActivity
import com.expedia.ui.CarWebViewActivity
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowApplication
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarWebViewTest {

    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()
    private val APP_VISITOR_ID_PARAM = "appvi="

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        Ui.getApplication(activity).defaultLaunchComponents()
        shadowApplication = ShadowApplication.getInstance()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebView() {

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppShowCarWebView)
        setPOSWithCarWebViewEnabled()
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)
        assertEquals(getCarUrlWithVisitorId("https://www.expedia.co.uk/car-hire?mcicid=App.Cars.WebView"), intentUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOff() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppShowCarWebView)
        setPOSWithCarWebViewEnabled()
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(CarActivity::class.java.name, intent.component.className)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensWebViewTvly() {

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppShowCarWebViewTvly)
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)
        assertEquals(getCarUrlWithVisitorId("https://www.travelocity.com/Cars?mcicid=App.Cars.WebView"), intentUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensNativeAbTestOffTvly() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppShowCarWebViewTvly)
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(CarActivity::class.java.name, intent.component.className)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun carsLaunchButtonOpensWebViewOrbitz() {

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppShowCarWebViewOrbitz)
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)
        assertEquals(getCarUrlWithVisitorId("https://www.orbitz.com/Cars?mcicid=App.Cars.WebView"), intentUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun carsLaunchButtonOpensNativeAbTestOffOrbitz() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppShowCarWebViewOrbitz)
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(CarActivity::class.java.name, intent.component.className)
    }

    private fun goToCars() {
        NavUtils.goToCars(activity, null)
        activity.finish()
    }

    private fun setPOSWithCarWebViewEnabled() {
        SettingUtils.save(activity, R.string.PointOfSaleKey, PointOfSaleId.UNITED_KINGDOM.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getCarUrlWithVisitorId(baseUrl: String): String {
        val visitorID = ADMS_Measurement.sharedInstance().visitorID
        return baseUrl + "&" + APP_VISITOR_ID_PARAM + visitorID
    }

}
