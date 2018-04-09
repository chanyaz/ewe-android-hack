package com.expedia.util

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageUtilTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSVariant1() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusVariant.ONE.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSVariant2() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusVariant.TWO.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSControl() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusVariant.CONTROL.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Bundle Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKVariant1() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusVariant.ONE.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKVariant2() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusVariant.TWO.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKControl() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusVariant.CONTROL.value,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Bundle Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringSingapore() {
        assertPackageTitle(posId = PointOfSaleId.SINGAPORE,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringFrance() {
        assertPackageTitle(posId = PointOfSaleId.FRANCE,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringItaly() {
        assertPackageTitle(posId = PointOfSaleId.ITALY,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringMalaysia() {
        assertPackageTitle(posId = PointOfSaleId.MALAYSIA,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringAustralia() {
        assertPackageTitle(posId = PointOfSaleId.AUSTRALIA,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringNewZealand() {
        assertPackageTitle(posId = PointOfSaleId.NEW_ZEALND,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringJapan() {
        assertPackageTitle(posId = PointOfSaleId.JAPAN,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringCanada() {
        assertPackageTitle(posId = PointOfSaleId.CANADA,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Flight + Hotel")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringSouthKorea() {
        assertPackageTitle(posId = PointOfSaleId.SOUTH_KOREA,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringMexico() {
        assertPackageTitle(posId = PointOfSaleId.MEXICO,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringFallback() {
        assertPackageTitle(posId = PointOfSaleId.FINLAND,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Bundle Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageUNITED_STATESLOBDisabled() {
        RoboTestHelper.setPOS(PointOfSaleId.UNITED_STATES)
        assertFalse(PackageUtil.isPackageLOBUnderABTest)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun testPackageEBOOKERS_GERMANYLOBEnabled() {
        RoboTestHelper.setPOS(PointOfSaleId.EBOOKERS_GERMANY)
        assertTrue(PackageUtil.isPackageLOBUnderABTest)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.AIRASIAGO))
    fun testPackageLOBTitleStringAAG_TH() {
        assertPackageTitle(posId = PointOfSaleId.AIRASIAGO_THAILAND,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EBOOKERS))
    fun testPackageLOBTitleStringEbookers_GERMANY() {
        assertPackageTitle(posId = PointOfSaleId.EBOOKERS_GERMANY,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleHotelDatesGuestText() {
        assertEquals("Mar 7 - Mar 10, 3 guests", PackageUtil.getBundleHotelDatesAndGuestsText(context, "2021-03-07", "2021-03-10", 3))
    }

    private fun updateABTestVariant(value: Int) {
        AbacusTestUtils.bucketTests(AbacusUtils.PackagesTitleChange)
        RoboTestHelper.updateABTest(AbacusUtils.PackagesTitleChange, value)
    }

    private fun assertPackageTitle(posId: PointOfSaleId, abTestValue: Int? = null, expectedPackagesLobTitleABTestEnabled: Boolean, expectedPackageTitle: String) {
        RoboTestHelper.setPOS(posId)
        abTestValue?.let {
            updateABTestVariant(abTestValue)
        }
        assertEquals(expectedPackagesLobTitleABTestEnabled, PackageUtil.isPackagesLobTitleABTestEnabled)
        assertEquals(expectedPackageTitle, RoboTestHelper.getContext().getString(PackageUtil.packageTitle(RoboTestHelper.getContext())))
    }
}
