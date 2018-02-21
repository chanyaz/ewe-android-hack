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
import org.joda.time.DateTime
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
    private val featureConfig = ProductFlavorFeatureConfiguration.getInstance()
    private val shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application)

    @Test
    fun testIsAppCrossSellInActivityShareContentEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "ebookers" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "expedia" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "lastMinute" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "mrJet" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "orbitz" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "travelocity" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "voyages" -> assertFalse(featureConfig.isAppCrossSellInActivityShareContentEnabled)
            "wotif" -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
        }
    }

    @Test
    fun testIsAppCrossSellInCarShareContentEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "ebookers" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "expedia" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "lastMinute" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "mrJet" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "orbitz" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "travelocity" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "voyages" -> assertFalse(featureConfig.isAppCrossSellInCarShareContentEnabled)
            "wotif" -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
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
            "airAsiaGo" -> assertTrue(featureConfig.isTuneEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isTuneEnabled)
            "ebookers" -> assertTrue(featureConfig.isTuneEnabled)
            "expedia" -> assertTrue(featureConfig.isTuneEnabled)
            "lastMinute" -> assertTrue(featureConfig.isTuneEnabled)
            "mrJet" -> assertTrue(featureConfig.isTuneEnabled)
            "orbitz" -> assertTrue(featureConfig.isTuneEnabled)
            "travelocity" -> assertTrue(featureConfig.isTuneEnabled)
            "voyages" -> assertFalse(featureConfig.isTuneEnabled)
            "wotif" -> assertTrue(featureConfig.isTuneEnabled)
        }
    }

    @Test
    fun testIsFacebookLoginIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookLoginIntegrationEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "ebookers" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "expedia" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "lastMinute" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "mrJet" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "orbitz" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "travelocity" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
            "voyages" -> assertFalse(featureConfig.isFacebookLoginIntegrationEnabled)
            "wotif" -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
        }
    }

    @Test
    fun testIsFacebookShareIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookShareIntegrationEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "ebookers" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "expedia" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "lastMinute" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "mrJet" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "orbitz" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "travelocity" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
            "voyages" -> assertFalse(featureConfig.isFacebookShareIntegrationEnabled)
            "wotif" -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
        }
    }

    @Test
    fun testIsAppIntroEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppIntroEnabled)
            "cheapTickets" -> assertFalse(featureConfig.isAppIntroEnabled)
            "ebookers" -> assertFalse(featureConfig.isAppIntroEnabled)
            "expedia" -> assertTrue(featureConfig.isAppIntroEnabled)
            "lastMinute" -> assertFalse(featureConfig.isAppIntroEnabled)
            "mrJet" -> assertFalse(featureConfig.isAppIntroEnabled)
            "orbitz" -> assertFalse(featureConfig.isAppIntroEnabled)
            "travelocity" -> assertFalse(featureConfig.isAppIntroEnabled)
            "voyages" -> assertFalse(featureConfig.isAppIntroEnabled)
            "wotif" -> assertFalse(featureConfig.isAppIntroEnabled)
        }
    }

    @Test
    fun testGetLaunchScreenActionLogo() {
        when (brand) {
            "airAsiaGo" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "cheapTickets" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "ebookers" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "expedia" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "lastMinute" -> assertNotEquals(0, featureConfig.launchScreenActionLogo)
            "mrJet" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "orbitz" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "travelocity" -> assertEquals(0, featureConfig.launchScreenActionLogo)
            "voyages" -> assertNotEquals(0, featureConfig.launchScreenActionLogo)
            "wotif" -> assertNotEquals(0, featureConfig.launchScreenActionLogo)
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
            "airAsiaGo" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "ebookers" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "expedia" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "lastMinute" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "mrJet" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "orbitz" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "travelocity" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
            "voyages" -> assertFalse(featureConfig.isFacebookTrackingEnabled)
            "wotif" -> assertTrue(featureConfig.isFacebookTrackingEnabled)
        }
    }

    @Test
    fun testIsAbacusTestEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAbacusTestEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "ebookers" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "expedia" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "lastMinute" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "mrJet" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "orbitz" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "travelocity" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "voyages" -> assertTrue(featureConfig.isAbacusTestEnabled)
            "wotif" -> assertTrue(featureConfig.isAbacusTestEnabled)
        }
    }

    @Test
    fun testGetRewardsLayoutId() {
        when (brand) {
            "airAsiaGo" -> assertEquals(0, featureConfig.rewardsLayoutId)
            "cheapTickets" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "ebookers" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "expedia" -> assertEquals(R.layout.pay_with_points_widget_stub, featureConfig.rewardsLayoutId)
            "lastMinute" -> assertEquals(0, featureConfig.rewardsLayoutId)
            "mrJet" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "orbitz" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "travelocity" -> assertEquals(0, featureConfig.rewardsLayoutId)
            "voyages" -> assertEquals(0, featureConfig.rewardsLayoutId)
            "wotif" -> assertEquals(0, featureConfig.rewardsLayoutId)
        }
    }

    @Test
    fun testIsRewardProgramPointsType() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "cheapTickets" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "ebookers" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "expedia" -> assertTrue(featureConfig.isRewardProgramPointsType)
            "lastMinute" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "mrJet" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "orbitz" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "travelocity" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "voyages" -> assertFalse(featureConfig.isRewardProgramPointsType)
            "wotif" -> assertFalse(featureConfig.isRewardProgramPointsType)
        }
    }

    @Test
    fun testGetRewardTierAPINames() {
        val rewardTierAPINamesCTX = arrayOf("SILVER", "GOLD", "PLATINUM")
        val rewardTierAPINamesExp = arrayOf("BLUE", "SILVER", "GOLD")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.rewardTierAPINames)
            "cheapTickets" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.rewardTierAPINames)
            "ebookers" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.rewardTierAPINames)
            "expedia" -> assertArrayEquals(rewardTierAPINamesExp, featureConfig.rewardTierAPINames)
            "lastMinute" -> assertNull(featureConfig.rewardTierAPINames)
            "mrJet" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.rewardTierAPINames)
            "orbitz" -> assertArrayEquals(rewardTierAPINamesCTX, featureConfig.rewardTierAPINames)
            "travelocity" -> assertNull(featureConfig.rewardTierAPINames)
            "voyages" -> assertNull(featureConfig.rewardTierAPINames)
            "wotif" -> assertNull(featureConfig.rewardTierAPINames)
        }
    }

    @Test
    fun testGetRewardTierSupportNumberConfigNames() {
        val rewardTierSupportPhoneNumberConfigNamesOrbitz = arrayOf("supportPhoneNumberSilver", "supportPhoneNumberGold", "supportPhoneNumberPlatinum")
        val rewardTierSupportPhoneNumberConfigNamesExpedia = arrayOf("supportPhoneNumber", "supportPhoneNumberSilver", "supportPhoneNumberGold")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
            "cheapTickets" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.rewardTierSupportNumberConfigNames)
            "ebookers" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.rewardTierSupportNumberConfigNames)
            "expedia" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesExpedia, featureConfig.rewardTierSupportNumberConfigNames)
            "lastMinute" -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
            "mrJet" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.rewardTierSupportNumberConfigNames)
            "orbitz" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOrbitz, featureConfig.rewardTierSupportNumberConfigNames)
            "travelocity" -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
            "voyages" -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
            "wotif" -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
        }
    }

    @Test
    fun testGetRewardTierSupportEmailConfigNames() {
        val rewardTierSupportEmailConfigNamesExpedia = arrayOf(null, "supportEmailSilver", "supportEmailGold")
        when (brand) {
            "airAsiaGo" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "cheapTickets" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "ebookers" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "expedia" -> assertArrayEquals(rewardTierSupportEmailConfigNamesExpedia, featureConfig.rewardTierSupportEmailConfigNames)
            "lastMinute" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "mrJet" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "orbitz" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "travelocity" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "voyages" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
            "wotif" -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
        }
    }

    @Test
    fun testIsCommunicateSectionEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isCommunicateSectionEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "ebookers" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "expedia" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "lastMinute" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "mrJet" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "orbitz" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "travelocity" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
            "voyages" -> assertFalse(featureConfig.isCommunicateSectionEnabled)
            "wotif" -> assertTrue(featureConfig.isCommunicateSectionEnabled)
        }
    }

    @Test
    fun testGetUSPointOfSaleId() {
        when (brand) {
            "airAsiaGo" -> assertEquals(null, featureConfig.usPointOfSaleId)
            "cheapTickets" -> assertEquals(PointOfSaleId.CHEAPTICKETS, featureConfig.usPointOfSaleId)
            "ebookers" -> assertEquals(null, featureConfig.usPointOfSaleId)
            "expedia" -> assertEquals(PointOfSaleId.UNITED_STATES, featureConfig.usPointOfSaleId)
            "lastMinute" -> assertEquals(null, featureConfig.usPointOfSaleId)
            "mrJet" -> assertEquals(null, featureConfig.usPointOfSaleId)
            "orbitz" -> assertEquals(PointOfSaleId.ORBITZ, featureConfig.usPointOfSaleId)
            "travelocity" -> assertEquals(PointOfSaleId.TRAVELOCITY, featureConfig.usPointOfSaleId)
            "voyages" -> assertEquals(null, featureConfig.usPointOfSaleId)
            "wotif" -> assertEquals(null, featureConfig.usPointOfSaleId)
        }
    }

    @Test
    fun testIsGoogleAccountChangeEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "ebookers" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "expedia" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
            "lastMinute" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
            "mrJet" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "orbitz" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "travelocity" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
            "voyages" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
            "wotif" -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
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
            "airAsiaGo" -> assertNull(featureConfig.sharableFallbackImageURL)
            "expedia" -> assertEquals("http://media.expedia.com/mobiata/fb/exp-fb-share.png", featureConfig.sharableFallbackImageURL)
            "ebookers" -> assertNull(featureConfig.sharableFallbackImageURL)
            "cheapTickets" -> assertNull(featureConfig.sharableFallbackImageURL)
            "lastMinute" -> assertNull(featureConfig.sharableFallbackImageURL)
            "mrJet" -> assertNull(featureConfig.sharableFallbackImageURL)
            "orbitz" -> assertNull(featureConfig.sharableFallbackImageURL)
            "travelocity" -> assertNull(featureConfig.sharableFallbackImageURL)
            "voyages" -> assertNull(featureConfig.sharableFallbackImageURL)
            "wotif" -> assertNull(featureConfig.sharableFallbackImageURL)
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
            "airAsiaGo" -> assertFalse(featureConfig.isRateOurAppEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "ebookers" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "expedia" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "lastMinute" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "mrJet" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "orbitz" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "travelocity" -> assertTrue(featureConfig.isRateOurAppEnabled)
            "voyages" -> assertFalse(featureConfig.isRateOurAppEnabled)
            "wotif" -> assertTrue(featureConfig.isRateOurAppEnabled)
        }
    }

    @Test
    fun testIsRewardsCardEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "cheapTickets" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "ebookers" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "expedia" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "lastMinute" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "mrJet" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "orbitz" -> assertTrue(featureConfig.isRewardsCardEnabled)
            "travelocity" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "voyages" -> assertFalse(featureConfig.isRewardsCardEnabled)
            "wotif" -> assertFalse(featureConfig.isRewardsCardEnabled)
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
            "airAsiaGo" -> assertFalse(featureConfig.isFirebaseEnabled)
            "cheapTickets" -> assertFalse(featureConfig.isFirebaseEnabled)
            "ebookers" -> assertFalse(featureConfig.isFirebaseEnabled)
            "expedia" -> assertFalse(featureConfig.isFirebaseEnabled)
            "lastMinute" -> assertFalse(featureConfig.isFirebaseEnabled)
            "mrJet" -> assertFalse(featureConfig.isFirebaseEnabled)
            "orbitz" -> assertFalse(featureConfig.isFirebaseEnabled)
            "travelocity" -> assertFalse(featureConfig.isFirebaseEnabled)
            "voyages" -> assertFalse(featureConfig.isFirebaseEnabled)
            "wotif" -> assertFalse(featureConfig.isFirebaseEnabled)
        }
    }

    @Test
    fun testIsCarnivalEnabled() {
        when (brand) {
            "airAsiaGo" -> assertTrue(featureConfig.isCarnivalEnabled)
            "cheapTickets" -> assertTrue(featureConfig.isCarnivalEnabled)
            "ebookers" -> assertTrue(featureConfig.isCarnivalEnabled)
            "expedia" -> assertTrue(featureConfig.isCarnivalEnabled)
            "lastMinute" -> assertTrue(featureConfig.isCarnivalEnabled)
            "mrJet" -> assertTrue(featureConfig.isCarnivalEnabled)
            "orbitz" -> assertTrue(featureConfig.isCarnivalEnabled)
            "travelocity" -> assertTrue(featureConfig.isCarnivalEnabled)
            "voyages" -> assertFalse(featureConfig.isCarnivalEnabled)
            "wotif" -> assertTrue(featureConfig.isCarnivalEnabled)
        }
    }

    @Test
    fun testIsRecaptchaEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "cheapTickets" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "ebookers" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "expedia" -> assertTrue(featureConfig.isRecaptchaEnabled)
            "lastMinute" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "mrJet" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "orbitz" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "travelocity" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "voyages" -> assertFalse(featureConfig.isRecaptchaEnabled)
            "wotif" -> assertFalse(featureConfig.isRecaptchaEnabled)
        }
    }

    @Test
    fun testContactUsViaWeb() {
        featureConfig.contactUsViaWeb(context)
        val intent = shadowApplication.nextStartedActivity
        val data = intent.data
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
        val appCopyrightURL = PointOfSale.getPointOfSale().websiteUrl
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
            "airAsiaGo" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "cheapTickets" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "ebookers" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "expedia" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "lastMinute" -> {
                setPOS(PointOfSaleId.LASTMINUTE)
                assertNotEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            }
            "mrJet" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "orbitz" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "travelocity" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "voyages" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            "wotif" -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
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

    @Test
    fun testGetAppNameForMobiataPushNameHeader() {
        when (brand) {
            "airAsiaGo" -> assertEquals("AAGBookings", featureConfig.appNameForMobiataPushNameHeader)
            "cheapTickets" -> assertEquals("CTBookings", featureConfig.appNameForMobiataPushNameHeader)
            "ebookers" -> assertEquals("EbookersBookings", featureConfig.appNameForMobiataPushNameHeader)
            "expedia" -> assertEquals("ExpediaBookings", featureConfig.appNameForMobiataPushNameHeader)
            "lastMinute" -> assertEquals("LMBookings", featureConfig.appNameForMobiataPushNameHeader)
            "mrJet" -> assertEquals("MrJetBookings", featureConfig.appNameForMobiataPushNameHeader)
            "orbitz" -> assertEquals("OrbitzBookings", featureConfig.appNameForMobiataPushNameHeader)
            "travelocity" -> assertEquals("TvlyBookings", featureConfig.appNameForMobiataPushNameHeader)
            "voyages" -> assertEquals("VSCBookings", featureConfig.appNameForMobiataPushNameHeader)
            "wotif" -> assertEquals("WotifBookings", featureConfig.appNameForMobiataPushNameHeader)
        }
    }

    @Test
    fun testGetDefaultPOS() {
        when (brand) {
            "airAsiaGo" -> assertEquals(PointOfSaleId.AIRASIAGO_MALAYSIA, featureConfig.defaultPOS)
            "cheapTickets" -> assertEquals(PointOfSaleId.CHEAPTICKETS, featureConfig.defaultPOS)
            "ebookers" -> assertEquals(PointOfSaleId.EBOOKERS_UNITED_KINGDOM, featureConfig.defaultPOS)
            "expedia" -> assertEquals(PointOfSaleId.UNITED_KINGDOM, featureConfig.defaultPOS)
            "lastMinute" -> assertEquals(PointOfSaleId.LASTMINUTE, featureConfig.defaultPOS)
            "mrJet" -> assertEquals(PointOfSaleId.MRJET_SWEDEN, featureConfig.defaultPOS)
            "orbitz" -> assertEquals(PointOfSaleId.ORBITZ, featureConfig.defaultPOS)
            "travelocity" -> assertEquals(PointOfSaleId.TRAVELOCITY, featureConfig.defaultPOS)
            "voyages" -> assertEquals(PointOfSaleId.VSC, featureConfig.defaultPOS)
            "wotif" -> assertEquals(PointOfSaleId.WOTIF, featureConfig.defaultPOS)
        }
    }

    @Test
    fun testFormatDateTimeForHotelUserReviews() {
        val datetime = DateTime(2018, 2, 21, 14, 26)
        val formattedDateTime = featureConfig.formatDateTimeForHotelUserReviews(context, datetime)

        when (brand) {
            "airAsiaGo" -> assertEquals("2/21", formattedDateTime)
            "cheapTickets" -> assertEquals("2/21/2018", formattedDateTime)
            "ebookers" -> assertEquals("21/02/2018", formattedDateTime)
            "expedia" -> assertEquals("2/21/2018", formattedDateTime)
            "lastMinute" -> assertEquals("21/02", formattedDateTime)
            "mrJet" -> assertEquals("2018-02-21", formattedDateTime)
            "orbitz" -> assertEquals("2/21/2018", formattedDateTime)
            "travelocity" -> assertEquals("2/21", formattedDateTime)
            "voyages" -> assertEquals("21/02/2018", formattedDateTime)
            "wotif" -> assertEquals("21/02", formattedDateTime)
        }
    }
}
