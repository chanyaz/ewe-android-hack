package com.expedia.util

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageUtilTest {

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSVariant1() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSVariant2() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUSControl() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_STATES,
                abTestValue = AbacusUtils.DefaultTwoVariant.CONTROL.ordinal,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Bundle Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKVariant1() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKVariant2() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal,
                expectedPackagesLobTitleABTestEnabled = true,
                expectedPackageTitle = "Hotel + Flight Deals")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringUKControl() {
        assertPackageTitle(posId = PointOfSaleId.UNITED_KINGDOM,
                abTestValue = AbacusUtils.DefaultTwoVariant.CONTROL.ordinal,
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
                expectedPackageTitle = "Hotel + Flight")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageLOBTitleStringNewZealand() {
        assertPackageTitle(posId = PointOfSaleId.NEW_ZEALND,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Hotel + Flight")
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
    fun testPackageLOBTitleStringFallback() {
        assertPackageTitle(posId = PointOfSaleId.FINLAND,
                expectedPackagesLobTitleABTestEnabled = false,
                expectedPackageTitle = "Bundle Deals")
    }

    private fun updateABTestVariant(value: Int) {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesTitleChange)
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppPackagesTitleChange, value)
    }

    private fun assertPackageTitle(posId: PointOfSaleId, abTestValue: Int? = null, expectedPackagesLobTitleABTestEnabled: Boolean, expectedPackageTitle: String) {
        RoboTestHelper.setPOS(posId)
        abTestValue?.let {
            updateABTestVariant(abTestValue)
        }
        assertEquals(expectedPackagesLobTitleABTestEnabled, PackageUtil.isPackagesLobTitleABTestEnabled)
        assertEquals(expectedPackageTitle, RoboTestHelper.getContext().getString(PackageUtil.packageTitle))
    }
}