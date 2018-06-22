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
    fun testPackagesHighlightSortFilterDisabled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        assertFalse(isHighlightSortFilterOnPackagesEnabled(context))
    }

    @Test
    fun testPackagesHighlightSortFilterEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        assertTrue(isHighlightSortFilterOnPackagesEnabled(context))
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
    fun testRichContentForPackagesDisabled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesRichContent)
        assertFalse(isRichContentForPackagesEnabled(context))
    }

    @Test
    fun testRichContentForPackagesEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent)
        assertTrue(isRichContentForPackagesEnabled(context))
    }

    @Test
    fun testRichContentShowAmenityForPackagesDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 2)
        assertFalse(isRichContentShowAmenityForPackagesEnabled())
    }

    @Test
    fun testRichContentShowAmenityForPackagesEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 1)
        assertTrue(isRichContentShowAmenityForPackagesEnabled())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 3)
        assertTrue(isRichContentShowAmenityForPackagesEnabled())
    }

    @Test
    fun testRichContentShowRouteScoreForPackagesDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 1)
        assertFalse(isRichContentShowRouteScoreForPackagesEnabled())
    }

    @Test
    fun testRichContentShowRouteScoreForPackagesEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 2)
        assertTrue(isRichContentShowRouteScoreForPackagesEnabled())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesRichContent, 3)
        assertTrue(isRichContentShowRouteScoreForPackagesEnabled())
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
