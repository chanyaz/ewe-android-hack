package com.expedia.bookings.utils

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.features.Features
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// TODO: Move all FeatureUtil tests to this class (make an improvement card)
@RunWith(RobolectricRunner::class)
class FeatureUtilTest {

    private val context = RuntimeEnvironment.application

    @Test
    fun testEnableHotelMaterialForms() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppHotelMaterialForms)
        assertTrue(isHotelMaterialForms(context))
    }

    @Test
    fun testDisableHotelMaterialForms() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        assertFalse(isHotelMaterialForms(context))
    }

    @Test
    fun testIsLxWebViewCheckoutEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppLxWebCheckoutView)
        assertTrue(isLxWebViewCheckoutEnabled(context))
    }

    @Test
    fun testIsLxWebViewCheckoutDisabled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppLxWebCheckoutView)
        assertFalse(isLxWebViewCheckoutEnabled(context))
    }

    @Test
    fun testEnableBrandColors() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        assertTrue(isBrandColorEnabled(context))
    }

    @Test
    fun testDisableBrandColors() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        assertFalse(isBrandColorEnabled(context))
    }

    @Test
    fun testEnableTripFolders() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.TripFoldersFragment)
        assertTrue(checkIfTripFoldersEnabled(context))
    }

    @Test
    fun testDisableTripFolders() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripFoldersFragment)
        assertFalse(checkIfTripFoldersEnabled(context))
    }

    @Test
    fun testPackagesServerSideFilteringDisabled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        assertFalse(isServerSideFilteringEnabledForPackages(context))
    }

    @Test
    fun testPackagesServerSideFilteringEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        assertTrue(isServerSideFilteringEnabledForPackages(context))
    }

    @Test
    fun testPackagesBetterSavingsOnRDScreenDisabled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        assertFalse(isBetterSavingsOnRDScreenEnabledForPackages(context))
    }

    @Test
    fun testPackagesBetterSavingsOnRDScreenEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        assertTrue(isBetterSavingsOnRDScreenEnabledForPackages(context))
    }

    @Test
    fun testLXMultipleDatesSearchEnabled() {
        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertTrue(isLXMultipleDatesSearchEnabled())
    }

    @Test
    fun testLXMultipleDatesSearchDisabled() {
        FeatureTestUtils.disableFeature(context, Features.all.lxMultipleDatesSearch)
        assertFalse(isLXMultipleDatesSearchEnabled())
    }
}
