package com.expedia.bookings.test


import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.adobe.adms.measurement.ADMS_Measurement
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


    private fun goToCars() {
        NavUtils.goToCars(activity, null)
        activity.finish()
    }

    private fun setPOSWithCarWebViewEnabled(POSId: String) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, POSId)
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getCarUrlWithVisitorId(baseUrl: String): String {
        val visitorID = ADMS_Measurement.sharedInstance().visitorID
        return baseUrl + "&" + APP_VISITOR_ID_PARAM + visitorID
    }

    private fun verifyCarsWebViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")
        assertEquals(CarWebViewActivity::class.java.name, intent.component.className)
        assertEquals(getCarUrlWithVisitorId(PointOfSale.getPointOfSale().carsTabWebViewURL), intentUrl)
    }

    private fun verifyCarsAppViewIsLaunched() {
        goToCars()
        val intent = shadowApplication!!.nextStartedActivity
        assertEquals(CarActivity::class.java.name, intent.component.className)
    }

}
