package com.expedia.bookings.test

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.CarNavUtils
import com.expedia.ui.LOBWebViewActivity
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowApplication
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CarWebViewTest {

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
    fun carsLaunchButtonOpensWebViewUK() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensWebViewTvly() {
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewCA() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.CANADA.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensWebViewTVLYCA() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.TRAVELOCITY_CA.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.WOTIF))
    fun carsLaunchButtonOpensWebViewWotifAU() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.WOTIF.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewNZ() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.NEW_ZEALND.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun carsLaunchButtonOpensWebViewEBCH() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.EBOOKERS_SWITZERLAND.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun carsLaunchButtonOpensWebViewOB() {
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.CHEAPTICKETS))
    fun carsLaunchButtonOpensWebViewCT() {
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewDE() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.GERMANY.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewFR() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.FRANCE.id.toString())
        verifyCarsWebViewIsLaunched()
    }

    private fun goToCars() {
        CarNavUtils.goToCars(activity, 0)
        activity.finish()
    }

    private fun setPOSWithCarWebViewEnabled(POSId: String) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, POSId)
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun verifyCarsWebViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.startsWith(PointOfSale.getPointOfSale().carsTabWebViewURL))
        assertTrue(intentUrl.contains("&adobe_mc="))
    }

    private fun verifyCarsFlexViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)

        assertTrue(intentUrl.startsWith("https://www." + PointOfSale.getPointOfSale().getUrl() + "/carshomepage?mcicid=App.Cars.WebView"))
        assertTrue(intentUrl.contains("&adobe_mc="))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensFlexViewUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsFlexView)
        verifyCarsFlexViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewAbTestOffUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsFlexView)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewAR() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.ARGENTINA.id.toString())
        verifyCarsWebViewIsLaunched()
    }
}
