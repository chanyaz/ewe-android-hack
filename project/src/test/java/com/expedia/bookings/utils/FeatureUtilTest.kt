package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FeatureUtilTest {

    private val context = RuntimeEnvironment.application

    // Hotel Material Forms
    @Test
    fun testEnableHotelMaterialForms() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        assertTrue(isHotelMaterialForms(context))
    }

    @Test
    fun testDisableHotelMaterialForms() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        assertFalse(isHotelMaterialForms(context))
    }

    @Test
    fun testHotelMaterialFormsFeatureToggleOffABTestOn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        SettingUtils.save(context, context.getString(R.string.preference_enable_hotel_material_forms), false)
        assertFalse(isHotelMaterialForms(context))
    }

    @Test
    fun testHotelMaterialFormsFeatureToggleOnABTestOff() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        SettingUtils.save(context, context.getString(R.string.preference_enable_hotel_material_forms), true)
        assertFalse(isHotelMaterialForms(context))
    }

    // MID Checkout
    @Test
    fun testEnableMIDCheckout() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        assertTrue(isMIDCheckoutEnabled(context))
    }

    @Test
    fun testDisableMIDCheckout() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        assertFalse(isMIDCheckoutEnabled(context))
    }

    @Test
    fun testDisableMIDCheckoutFeatureToggleOffABTestOn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppMIDCheckout)
        SettingUtils.save(context, context.getString(R.string.preference_enable_mid_checkout), false)
        assertFalse(isMIDCheckoutEnabled(context))
    }

    @Test
    fun testMIDCheckoutFeatureToggleOnABTestOff() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppMIDCheckout)
        SettingUtils.save(context, context.getString(R.string.preference_enable_mid_checkout), true)
        assertFalse(isMIDCheckoutEnabled(context))
    }

    // Credit Card Messaging For Pay Later Hotel
    @Test
    fun testEnableCreditCardMessagingForPayLaterHotel() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging, R.string.pay_later_credit_card_messaging)
        assertTrue(isCreditCardMessagingForPayLaterEnabled(context))
    }

    @Test
    fun testDisableCreditCardMessagingForPayLaterHotel() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging, R.string.pay_later_credit_card_messaging)
        assertFalse(isCreditCardMessagingForPayLaterEnabled(context))
    }

    //
    @Test
    fun testEnableDisplayEligibleCardsOnPaymentForm() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
        assertTrue(isDisplayCardsOnPaymentForm(context))
    }

    @Test
    fun testDisableDisplayEligibleCardsOnPaymentForm() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
        assertFalse(isDisplayCardsOnPaymentForm(context))
    }

    //
    @Test
    fun testKrazyglueEnabledWithBucketVariantOne() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsKrazyglue, 1)
        SettingUtils.save(context, context.getString(R.string.preference_enable_krazy_glue_on_flights_confirmation), true)

        assertTrue(isKrazyglueOnFlightsConfirmationEnabled(context))
    }

    @Test
    fun testKrazyglueEnabledWithBucketVariantTwo() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsKrazyglue, 2)
        SettingUtils.save(context, context.getString(R.string.preference_enable_krazy_glue_on_flights_confirmation), true)

        assertTrue(isKrazyglueOnFlightsConfirmationEnabled(context))
    }

    @Test
    fun testKrazyglueDisabledBucketedFeatureToggleOff() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)
        SettingUtils.save(context, context.getString(R.string.preference_enable_krazy_glue_on_flights_confirmation), false)

        assertFalse(isKrazyglueOnFlightsConfirmationEnabled(context))
    }

    @Test
    fun testKrazyglueDisabledFeatureToggleEnabledNotBucketed() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)
        SettingUtils.save(context, context.getString(R.string.preference_enable_krazy_glue_on_flights_confirmation), true)

        assertFalse(isKrazyglueOnFlightsConfirmationEnabled(context))
    }

    //
    @Test
    fun testToggleOnShowFlightsCheckoutWebview() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview, R.string.preference_show_flights_checkout_webview)
        assertTrue(isShowFlightsCheckoutWebview(context))
    }

    @Test
    fun testToggleOffShowFlightsCheckoutWebview() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview, R.string.preference_show_flights_checkout_webview)
        assertFalse(isShowFlightsCheckoutWebview(context))
    }
}
