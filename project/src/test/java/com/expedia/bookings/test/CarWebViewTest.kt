package com.expedia.bookings.test


import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.ui.CarActivity
import com.expedia.ui.CarWebViewActivity
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
    fun carsLaunchButtonOpensWebView() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewUK)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOff() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_KINGDOM.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewUK)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensWebViewTvly() {
        RoboTestHelper.bucketTests(PointOfSale.getPointOfSale().carsWebViewABTestID)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensNativeAbTestOffTvly() {
        RoboTestHelper.controlTests(PointOfSale.getPointOfSale().carsWebViewABTestID)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewUS() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewUS)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOffUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewUS)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewCA() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.CANADA.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewCA)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOffCA() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.CANADA.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewCA)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensWebViewTVLYCA() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.TRAVELOCITY_CA.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewCA)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.TRAVELOCITY))
    fun carsLaunchButtonOpensNativeAbTestOffTVLYCA() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.TRAVELOCITY_CA.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewCA)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.WOTIF))
    fun carsLaunchButtonOpensWebViewWotifAU() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.WOTIF.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewAUNZ)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.WOTIF))
    fun carsLaunchButtonOpensNativeAbTestOffWotifAU() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.WOTIF.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewAUNZ)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewNZ() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.NEW_ZEALND.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewAUNZ)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOffNZ() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.NEW_ZEALND.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewAUNZ)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun carsLaunchButtonOpensWebViewEBCH() {

        setPOSWithCarWebViewEnabled(PointOfSaleId.EBOOKERS_SWITZERLAND.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewEB)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun carsLaunchButtonOpensNativeAbTestOffEBCH() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.EBOOKERS_SWITZERLAND.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewEB)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun carsLaunchButtonOpensWebViewOB() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewOB)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun carsLaunchButtonOpensNativeAbTestOffOB() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewOB)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.CHEAPTICKETS))
    fun carsLaunchButtonOpensWebViewCT() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewCT)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.CHEAPTICKETS))
    fun carsLaunchButtonOpensNativeAbTestOffCT() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewCT)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewDE() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.GERMANY.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewEMEA)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOffDE() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.GERMANY.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewEMEA)
        verifyCarsAppViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewFR() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.FRANCE.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewEMEA)
        verifyCarsWebViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensNativeAbTestOffFR() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.FRANCE.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsWebViewEMEA)
        verifyCarsAppViewIsLaunched()
    }

    private fun goToCars() {
        NavUtils.goToCars(activity, null)
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
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.startsWith(PointOfSale.getPointOfSale().carsTabWebViewURL))
        assertTrue(intentUrl.contains("&adobe_mc="))
    }

    private fun verifyCarsAppViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(CarActivity::class.java.name, intent.component.className)
    }

    private fun verifyCarsFlexViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)

        assertTrue(intentUrl.startsWith("https://www." + PointOfSale.getPointOfSale().getUrl() + "/carshomepage?mcicid=App.Cars.WebView"))
        assertTrue(intentUrl.contains("&adobe_mc="))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensFlexViewUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewUS, AbacusUtils.EBAndroidAppCarsFlexView)
        verifyCarsFlexViewIsLaunched()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun carsLaunchButtonOpensWebViewAbTestOffUS() {
        setPOSWithCarWebViewEnabled(PointOfSaleId.UNITED_STATES.id.toString())
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppCarsFlexView)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppCarsWebViewUS)
        verifyCarsWebViewIsLaunched()
    }

}
