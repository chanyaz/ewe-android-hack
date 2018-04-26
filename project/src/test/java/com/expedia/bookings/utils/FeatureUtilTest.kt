package com.expedia.bookings.utils

import com.expedia.bookings.data.abacus.AbacusUtils
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
}
