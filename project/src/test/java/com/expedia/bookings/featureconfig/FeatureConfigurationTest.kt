package com.expedia.bookings.featureconfig

import android.app.Application
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.server.EndPoint
import com.expedia.bookings.test.robolectric.RoboTestHelper.setPOS
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FeatureConfigurationTest {
    val brand = BuildConfig.FLAVOR
    val context: Application = RuntimeEnvironment.application
    val featureConfig = ProductFlavorFeatureConfiguration.getInstance()
    val shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application)

    @Test
    fun testIsAppCrossSellInActivityShareContentEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "ebookers" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "expedia" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "lastMinute" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "mrJet" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "orbitz" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "travelocity" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "voyages" -> assertFalse(featureConfig.isAppCrossSellInActivityShareContentEnabled())
            "wotif" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled())
        }
    }

    @Test
    fun testIsAppCrossSellInCarShareContentEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "ebookers" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "expedia" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "lastMinute" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "mrJet" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "orbitz" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "travelocity" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "voyages" -> assertFalse(featureConfig.isAppCrossSellInCarShareContentEnabled())
            "wotif" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled())
        }
    }

    @Test
    fun testShouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "cheapTickets" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "ebookers" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "expedia" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "lastMinute" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "mrJet" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "orbitz" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "travelocity" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "voyages" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            "wotif" -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
        }
    }

    @Test
    fun testWantsCustomHandlingForLocaleConfiguration() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "cheapTickets" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "ebookers" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "expedia" -> assertFalse(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "lastMinute" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "mrJet" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "orbitz" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "travelocity" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "voyages" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            "wotif" -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
        }
    }

    @Test
    fun testShouldUseDotlessDomain() {
        val endpoint = EndPoint.PRODUCTION
        val integrationEndpoint = EndPoint.INTEGRATION
        when (brand) {
            "airAsiaGo" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "cheapTickets" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "ebookers" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "expedia" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "lastMinute" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "mrJet" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "orbitz" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "travelocity" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "voyages" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
            "wotif" -> {
                assertFalse(featureConfig.shouldUseDotlessDomain(endpoint))
                assertTrue(featureConfig.shouldUseDotlessDomain(integrationEndpoint))
            }
        }
    }

    @Test
    fun testTouchupE3EndpointUrlIfRequired() {
        val e3EndPoint = "www.expedia.com"
        val expectedURL = "expedia.com"
        when (brand) {
            "airAsiaGo" -> {
                setPOS(PointOfSaleId.AIRASIAGO_THAILAND)
                assertEquals(expectedURL, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            }
            "cheapTickets" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "ebookers" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "expedia" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "lastMinute" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "mrJet" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "orbitz" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "travelocity" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "voyages" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
            "wotif" -> assertEquals(e3EndPoint, featureConfig.touchupE3EndpointUrlIfRequired(e3EndPoint))
        }
    }

    @Test
    fun testIsTuneEnabled() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.isTuneEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isTuneEnabled())
            "ebookers" -> assertTrue(featureConfig.isTuneEnabled())
            "expedia" -> assertTrue(featureConfig.isTuneEnabled())
            "lastMinute" -> assertTrue(featureConfig.isTuneEnabled())
            "mrJet" -> assertTrue(featureConfig.isTuneEnabled())
            "orbitz" -> assertTrue(featureConfig.isTuneEnabled())
            "travelocity" -> assertTrue(featureConfig.isTuneEnabled())
            "voyages" -> assertFalse(featureConfig.isTuneEnabled())
            "wotif" -> assertTrue(featureConfig.isTuneEnabled())
        }
    }

    @Test
    fun testIsFacebookLoginIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookLoginIntegrationEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "ebookers" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "expedia" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "lastMinute" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "mrJet" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "orbitz" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "travelocity" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
            "voyages" -> assertFalse(featureConfig.isFacebookLoginIntegrationEnabled())
            "wotif" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled())
        }
    }

    @Test
    fun testIsFacebookShareIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookShareIntegrationEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "ebookers" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "expedia" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "lastMinute" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "mrJet" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "orbitz" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "travelocity" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
            "voyages" -> assertFalse(featureConfig.isFacebookShareIntegrationEnabled())
            "wotif" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled())
        }
    }

    @Test
    fun testIsAppIntroEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppIntroEnabled())
            "cheapTickets" -> assertFalse(featureConfig.isAppIntroEnabled())
            "ebookers" -> assertFalse(featureConfig.isAppIntroEnabled())
            "expedia" -> assertTrue(featureConfig.isAppIntroEnabled())
            "lastMinute" -> assertFalse(featureConfig.isAppIntroEnabled())
            "mrJet" -> assertFalse(featureConfig.isAppIntroEnabled())
            "orbitz" -> assertFalse(featureConfig.isAppIntroEnabled())
            "travelocity" -> assertFalse(featureConfig.isAppIntroEnabled())
            "voyages" -> assertFalse(featureConfig.isAppIntroEnabled())
            "wotif" -> assertFalse(featureConfig.isAppIntroEnabled())
        }
    }

    @Test
    fun testGetLaunchScreenActionLogo() {
        when (brand) {
            "airAsiaGo" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "cheapTickets" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "ebookers" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "expedia" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "lastMinute" -> assertNotEquals(0, featureConfig.getLaunchScreenActionLogo())
            "mrJet" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "orbitz" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "travelocity" -> assertEquals(0, featureConfig.getLaunchScreenActionLogo())
            "voyages" -> assertNotEquals(0, featureConfig.getLaunchScreenActionLogo())
            "wotif" -> assertNotEquals(0, featureConfig.getLaunchScreenActionLogo())
        }
    }

    @Test
    fun testGetPOSSpecificBrandName() {
        val brandName = BuildConfig.brand
        when (brand) {
            "airAsiaGo" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "cheapTickets" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "ebookers" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "expedia" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "lastMinute" -> assertNotEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "mrJet" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "orbitz" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "travelocity" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "voyages" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            "wotif" -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
        }
    }

    @Test
    fun testIsFacebookTrackingEnabled() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "ebookers" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "expedia" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "lastMinute" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "mrJet" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "orbitz" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "travelocity" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
            "voyages" -> assertFalse(featureConfig.isFacebookTrackingEnabled())
            "wotif" -> assertTrue(featureConfig.isFacebookTrackingEnabled())
        }
    }

    @Test
    fun testIsAbacusTestEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAbacusTestEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "ebookers" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "expedia" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "lastMinute" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "mrJet" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "orbitz" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "travelocity" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "voyages" -> assertTrue(featureConfig.isAbacusTestEnabled())
            "wotif" -> assertTrue(featureConfig.isAbacusTestEnabled())
        }
    }

    @Test
    fun testGetRewardsLayoutId() {
        when (brand) {
            "airAsiaGo" -> assertEquals(0, featureConfig.getRewardsLayoutId())
            "cheapTickets" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.getRewardsLayoutId())
            "ebookers" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.getRewardsLayoutId())
            "expedia" -> assertEquals(R.layout.pay_with_points_widget_stub, featureConfig.getRewardsLayoutId())
            "lastMinute" -> assertEquals(0, featureConfig.getRewardsLayoutId())
            "mrJet" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.getRewardsLayoutId())
            "orbitz" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.getRewardsLayoutId())
            "travelocity" -> assertEquals(0, featureConfig.getRewardsLayoutId())
            "voyages" -> assertEquals(0, featureConfig.getRewardsLayoutId())
            "wotif" -> assertEquals(0, featureConfig.getRewardsLayoutId())
        }
    }

    @Test
    fun testIsRewardProgramPointsType() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "cheapTickets" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "ebookers" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "expedia" -> assertTrue(featureConfig.isRewardProgramPointsType())
            "lastMinute" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "mrJet" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "orbitz" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "travelocity" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "voyages" -> assertFalse(featureConfig.isRewardProgramPointsType())
            "wotif" -> assertFalse(featureConfig.isRewardProgramPointsType())
        }
    }

    @Test
    fun testGetRewardTierAPINames() {
        val rewardTierAPINamesCTX = arrayOf("SILVER", "GOLD", "PLATINUM")
        val rewardTierAPINamesExp = arrayOf("BLUE", "SILVER", "GOLD")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.getRewardTierAPINames())
            "cheapTickets" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.getRewardTierAPINames())
            "ebookers" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.getRewardTierAPINames())
            "expedia" -> assertArrayEquals(rewardTierAPINamesExp, featureConfig.getRewardTierAPINames())
            "lastMinute" -> assertNull(featureConfig.getRewardTierAPINames())
            "mrJet" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.getRewardTierAPINames())
            "orbitz" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.getRewardTierAPINames())
            "travelocity" -> assertNull(featureConfig.getRewardTierAPINames())
            "voyages" -> assertNull(featureConfig.getRewardTierAPINames())
            "wotif" -> assertNull(featureConfig.getRewardTierAPINames())
        }
    }

    @Test
    fun testGetRewardTierSupportNumberConfigNames() {
        val rewardTierSupportPhoneNumberConfigNamesOrbitz = arrayOf("supportPhoneNumberSilver", "supportPhoneNumberGold", "supportPhoneNumberPlatinum")
        val rewardTierSupportPhoneNumberConfigNamesExpedia = arrayOf("supportPhoneNumber", "supportPhoneNumberSilver", "supportPhoneNumberGold")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.getRewardTierSupportNumberConfigNames())
            "cheapTickets" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.getRewardTierSupportNumberConfigNames())
            "ebookers" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.getRewardTierSupportNumberConfigNames())
            "expedia" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesExpedia, featureConfig.getRewardTierSupportNumberConfigNames())
            "lastMinute" -> assertNull(featureConfig.getRewardTierSupportNumberConfigNames())
            "mrJet" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.getRewardTierSupportNumberConfigNames())
            "orbitz" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.getRewardTierSupportNumberConfigNames())
            "travelocity" -> assertNull(featureConfig.getRewardTierSupportNumberConfigNames())
            "voyages" -> assertNull(featureConfig.getRewardTierSupportNumberConfigNames())
            "wotif" -> assertNull(featureConfig.getRewardTierSupportNumberConfigNames())
        }
    }

    @Test
    fun testGetRewardTierSupportEmailConfigNames() {
        val rewardTierSupportEmailConfigNamesExpedia = arrayOf<String?>(null, "supportEmailSilver", "supportEmailGold")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "cheapTickets" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "ebookers" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "expedia" -> assertArrayEquals(rewardTierSupportEmailConfigNamesExpedia, featureConfig.getRewardTierSupportEmailConfigNames())
            "lastMinute" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "mrJet" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "orbitz" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "travelocity" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "voyages" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
            "wotif" -> assertNull(featureConfig.getRewardTierSupportEmailConfigNames())
        }
    }

    @Test
    fun testIsCommunicateSectionEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isCommunicateSectionEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "ebookers" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "expedia" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "lastMinute" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "mrJet" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "orbitz" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "travelocity" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
            "voyages" -> assertFalse(featureConfig.isCommunicateSectionEnabled())
            "wotif" -> assertTrue(featureConfig.isCommunicateSectionEnabled())
        }
    }

    @Test
    fun testGetUSPointOfSaleId() {
        when (brand) {
            "airAsiaGo" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
            "cheapTickets" -> assertEquals(PointOfSaleId.CHEAPTICKETS, featureConfig.getUSPointOfSaleId())
            "ebookers" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
            "expedia" -> assertEquals(PointOfSaleId.UNITED_STATES, featureConfig.getUSPointOfSaleId())
            "lastMinute" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
            "mrJet" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
            "orbitz" -> assertEquals(PointOfSaleId.ORBITZ, featureConfig.getUSPointOfSaleId())
            "travelocity" -> assertEquals(PointOfSaleId.TRAVELOCITY, featureConfig.getUSPointOfSaleId())
            "voyages" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
            "wotif" -> assertEquals(null, featureConfig.getUSPointOfSaleId())
        }
    }

    @Test
    fun testIsGoogleAccountChangeEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled())
            "ebookers" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled())
            "expedia" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
            "lastMinute" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
            "mrJet" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled())
            "orbitz" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled())
            "travelocity" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
            "voyages" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
            "wotif" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled())
        }
    }

    @Test
    fun testGetOmnitureEventValue() {
        val rewardNameKey = OmnitureTracking.OmnitureEventName.REWARD_PROGRAM_NAME
        when (brand) {
            "airAsiaGo" -> assertEquals(null, featureConfig.getOmnitureEventValue(rewardNameKey))
            "cheapTickets" -> assertEquals("cheaptickets", featureConfig.getOmnitureEventValue(rewardNameKey))
            "ebookers" -> assertEquals("ebookers", featureConfig.getOmnitureEventValue(rewardNameKey))
            "expedia" -> assertEquals("expedia", featureConfig.getOmnitureEventValue(rewardNameKey))
            "lastMinute" -> assertEquals(null, featureConfig.getOmnitureEventValue(rewardNameKey))
            "mrJet" -> assertEquals("mrjet", featureConfig.getOmnitureEventValue(rewardNameKey))
            "orbitz" -> assertEquals("orbitz", featureConfig.getOmnitureEventValue(rewardNameKey))
            "travelocity" -> assertEquals(null, featureConfig.getOmnitureEventValue(rewardNameKey))
            "voyages" -> assertEquals(null, featureConfig.getOmnitureEventValue(rewardNameKey))
            "wotif" -> assertEquals(null, featureConfig.getOmnitureEventValue(rewardNameKey))
        }
    }

    @Test
    fun testShouldShowMemberTier() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.shouldShowMemberTier())
            "cheapTickets" -> assertFalse(featureConfig.shouldShowMemberTier())
            "ebookers" -> assertTrue(featureConfig.shouldShowMemberTier())
            "expedia" -> assertTrue(featureConfig.shouldShowMemberTier())
            "lastMinute" -> assertTrue(featureConfig.shouldShowMemberTier())
            "mrJet" -> assertTrue(featureConfig.shouldShowMemberTier())
            "orbitz" -> assertTrue(featureConfig.shouldShowMemberTier())
            "travelocity" -> assertTrue(featureConfig.shouldShowMemberTier())
            "voyages" -> assertTrue(featureConfig.shouldShowMemberTier())
            "wotif" -> assertTrue(featureConfig.shouldShowMemberTier())
        }
    }

    @Test
    fun testGetSharableFallbackImageURL() {
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "expedia" -> assertEquals("http://media.expedia.com/mobiata/fb/exp-fb-share.png", featureConfig.getSharableFallbackImageURL())
            "ebookers" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "cheapTickets" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "lastMinute" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "mrJet" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "orbitz" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "travelocity" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "voyages" -> assertNull(featureConfig.getSharableFallbackImageURL())
            "wotif" -> assertNull(featureConfig.getSharableFallbackImageURL())
        }
    }

    @Test
    fun testShouldDisplayItinTrackAppLink() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "cheapTickets" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            "ebookers" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            "expedia" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "lastMinute" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "mrJet" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            "orbitz" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "travelocity" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "voyages" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
            "wotif" -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
        }
    }

    @Test
    fun testShouldSetExistingUserForTune() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "cheapTickets" -> assertTrue(featureConfig.shouldSetExistingUserForTune())
            "ebookers" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "expedia" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "lastMinute" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "mrJet" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "orbitz" -> assertTrue(featureConfig.shouldSetExistingUserForTune())
            "travelocity" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "voyages" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
            "wotif" -> assertFalse(featureConfig.shouldSetExistingUserForTune())
        }
    }

    @Test
    fun testShouldShowItinShare() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.shouldShowItinShare())
            "cheapTickets" -> assertFalse(featureConfig.shouldShowItinShare())
            "ebookers" -> assertFalse(featureConfig.shouldShowItinShare())
            "expedia" -> assertTrue(featureConfig.shouldShowItinShare())
            "lastMinute" -> assertTrue(featureConfig.shouldShowItinShare())
            "mrJet" -> assertFalse(featureConfig.shouldShowItinShare())
            "orbitz" -> assertTrue(featureConfig.shouldShowItinShare())
            "travelocity" -> assertTrue(featureConfig.shouldShowItinShare())
            "voyages" -> assertTrue(featureConfig.shouldShowItinShare())
            "wotif" -> assertTrue(featureConfig.shouldShowItinShare())
        }
    }

    @Test
    fun testIsRateOurAppEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRateOurAppEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "ebookers" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "expedia" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "lastMinute" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "mrJet" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "orbitz" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "travelocity" -> assertTrue(featureConfig.isRateOurAppEnabled())
            "voyages" -> assertFalse(featureConfig.isRateOurAppEnabled())
            "wotif" -> assertTrue(featureConfig.isRateOurAppEnabled())
        }
    }

    @Test
    fun testIsRewardsCardEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "cheapTickets" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "ebookers" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "expedia" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "lastMinute" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "mrJet" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "orbitz" -> assertTrue(featureConfig.isRewardsCardEnabled())
            "travelocity" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "voyages" -> assertFalse(featureConfig.isRewardsCardEnabled())
            "wotif" -> assertFalse(featureConfig.isRewardsCardEnabled())
        }
    }

    @Test
    fun testShowUserRewardsEnrollmentCheck() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
            "cheapTickets" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "ebookers" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "expedia" -> assertEquals(PointOfSale.getPointOfSale().shouldShowRewards(), featureConfig.showUserRewardsEnrollmentCheck())
            "lastMinute" -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
            "mrJet" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "orbitz" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "travelocity" -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
            "voyages" -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
            "wotif" -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
        }
    }

    @Test
    fun testSendEapidToTuneTracking() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "cheapTickets" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "ebookers" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "expedia" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "lastMinute" -> assertTrue(featureConfig.sendEapidToTuneTracking())
            "mrJet" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "orbitz" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "travelocity" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "voyages" -> assertFalse(featureConfig.sendEapidToTuneTracking())
            "wotif" -> assertFalse(featureConfig.sendEapidToTuneTracking())
        }
    }

    @Test
    fun testShouldShowPackageIncludesView() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "cheapTickets" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "ebookers" -> assertFalse(featureConfig.shouldShowPackageIncludesView())
            "expedia" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "lastMinute" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "mrJet" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "orbitz" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "travelocity" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "voyages" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
            "wotif" -> assertTrue(featureConfig.shouldShowPackageIncludesView())
        }
    }

    @Test
    fun testShowHotelLoyaltyEarnMessage() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "cheapTickets" -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
            "ebookers" -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
            "expedia" -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
            "lastMinute" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "mrJet" -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
            "orbitz" -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
            "travelocity" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "voyages" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "wotif" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
        }
    }

    @Test
    fun testShouldShowUserReview() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.shouldShowUserReview())
            "cheapTickets" -> assertTrue(featureConfig.shouldShowUserReview())
            "ebookers" -> assertTrue(featureConfig.shouldShowUserReview())
            "expedia" -> assertTrue(featureConfig.shouldShowUserReview())
            "lastMinute" -> assertTrue(featureConfig.shouldShowUserReview())
            "mrJet" -> assertTrue(featureConfig.shouldShowUserReview())
            "orbitz" -> assertTrue(featureConfig.shouldShowUserReview())
            "travelocity" -> assertTrue(featureConfig.shouldShowUserReview())
            "voyages" -> assertFalse(featureConfig.shouldShowUserReview())
            "wotif" -> assertTrue(featureConfig.shouldShowUserReview())
        }
    }

    @Test
    fun testShouldShowVIPLoyaltyMessage() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "cheapTickets" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "ebookers" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "expedia" -> assertTrue(featureConfig.shouldShowVIPLoyaltyMessage())
            "lastMinute" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "mrJet" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "orbitz" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "travelocity" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "voyages" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
            "wotif" -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
        }
    }

    @Test
    fun testIsFirebaseEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFirebaseEnabled())
            "cheapTickets" -> assertFalse(featureConfig.isFirebaseEnabled())
            "ebookers" -> assertFalse(featureConfig.isFirebaseEnabled())
            "expedia" -> assertFalse(featureConfig.isFirebaseEnabled())
            "lastMinute" -> assertFalse(featureConfig.isFirebaseEnabled())
            "mrJet" -> assertFalse(featureConfig.isFirebaseEnabled())
            "orbitz" -> assertFalse(featureConfig.isFirebaseEnabled())
            "travelocity" -> assertFalse(featureConfig.isFirebaseEnabled())
            "voyages" -> assertFalse(featureConfig.isFirebaseEnabled())
            "wotif" -> assertFalse(featureConfig.isFirebaseEnabled())
        }
    }

    @Test
    fun testIsCarnivalEnabled() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.isCarnivalEnabled())
            "cheapTickets" -> assertTrue(featureConfig.isCarnivalEnabled())
            "ebookers" -> assertTrue(featureConfig.isCarnivalEnabled())
            "expedia" -> assertTrue(featureConfig.isCarnivalEnabled())
            "lastMinute" -> assertTrue(featureConfig.isCarnivalEnabled())
            "mrJet" -> assertTrue(featureConfig.isCarnivalEnabled())
            "orbitz" -> assertTrue(featureConfig.isCarnivalEnabled())
            "travelocity" -> assertTrue(featureConfig.isCarnivalEnabled())
            "voyages" -> assertFalse(featureConfig.isCarnivalEnabled())
            "wotif" -> assertTrue(featureConfig.isCarnivalEnabled())
        }
    }

    @Test
    fun testIsRecaptchaEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "cheapTickets" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "ebookers" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "expedia" -> assertTrue(featureConfig.isRecaptchaEnabled())
            "lastMinute" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "mrJet" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "orbitz" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "travelocity" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "voyages" -> assertFalse(featureConfig.isRecaptchaEnabled())
            "wotif" -> assertFalse(featureConfig.isRecaptchaEnabled())
        }
    }

    @Test
    fun testContactUsViaWeb() {
        featureConfig.contactUsViaWeb(context)
        val intent = shadowApplication.getNextStartedActivity()
        val data = intent.getData()
        when (brand) {
            "airAsiaGo" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "cheapTickets" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "ebookers" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "expedia" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "lastMinute" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "mrJet" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "orbitz" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "travelocity" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
            "voyages" -> assertTrue(intent.extras.getString("ARG_URL").contains("http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html"))
            "wotif" -> assertEquals(PointOfSale.getPointOfSale().bookingSupportUrl, data.toString())
        }
    }

    @Test
    fun testGetCopyrightLogoUrl() {
        val appCopyrightURL = PointOfSale.getPointOfSale().getWebsiteUrl()
        val appCopyrightURLVariant = context.getString(R.string.app_copyright_logo_url)
        when (brand) {
            "airAsiaGo" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "cheapTickets" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "ebookers" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "expedia" -> assertEquals(appCopyrightURLVariant, featureConfig.getCopyrightLogoUrl(context))
            "lastMinute" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "mrJet" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "orbitz" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "travelocity" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
            "voyages" -> assertEquals(appCopyrightURLVariant, featureConfig.getCopyrightLogoUrl(context))
            "wotif" -> assertEquals(appCopyrightURL, featureConfig.getCopyrightLogoUrl(context))
        }
    }

    @Test
    fun testGetPOSSpecificBrandLogo() {
        when (brand) {
            "airAsiaGo" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "cheapTickets" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "ebookers" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "expedia" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "lastMinute" -> {
                setPOS(PointOfSaleId.LASTMINUTE)
                assertNotEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            }
            "mrJet" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "orbitz" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "travelocity" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "voyages" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
            "wotif" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.getPOSSpecificBrandLogo())
        }
    }

    @Test
    fun testGetPosURLToShow() {
        val expectedURL = "www.sampleURL.com"
        val expectedURLVoyages = "agence" + expectedURL
        when (brand) {
            "airAsiaGo" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "cheapTickets" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "ebookers" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "expedia" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "lastMinute" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "mrJet" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "orbitz" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "travelocity" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
            "voyages" -> assertEquals(expectedURLVoyages, featureConfig.getPosURLToShow(expectedURL))
            "wotif" -> assertEquals(expectedURL, featureConfig.getPosURLToShow(expectedURL))
        }
    }

    @Test
    fun testGetRewardsCardUrl() {
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "expedia" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "ebookers" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "cheapTickets" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "lastMinute" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "mrJet" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "voyages" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "travelocity" -> assertNull(featureConfig.getRewardsCardUrl(context))
            "orbitz" -> assertEquals("http://www.orbitz.com/rewards/visacard", featureConfig.getRewardsCardUrl(context))
        }
    }
}
