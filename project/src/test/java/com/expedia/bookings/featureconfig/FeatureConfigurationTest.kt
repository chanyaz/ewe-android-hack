package com.expedia.bookings.featureconfig

import android.app.Application
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
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
            else -> assertTrue(featureConfig.isAppCrossSellInActivityShareContentEnabled)
        }
    }

    @Test
    fun testIsAppCrossSellInCarShareContentEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAppCrossSellInCarShareContentEnabled)
            else -> assertTrue(featureConfig.isAppCrossSellInCarShareContentEnabled)
        }
    }

    @Test
    fun testShouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
            else -> assertTrue(featureConfig.shouldDisplayInsuranceDetailsIfAvailableOnItinCard())
        }
    }

    @Test
    fun testWantsCustomHandlingForLocaleConfiguration() {
        when (brand) {
            "expedia" -> assertFalse(featureConfig.wantsCustomHandlingForLocaleConfiguration())
            else -> assertTrue(featureConfig.wantsCustomHandlingForLocaleConfiguration())
        }
    }

    @Test
    fun testTouchupE3EndpointUrlIfRequired() {
        val wwwUrl = "www.expedia.com"
        val urlWithoutWWW = "expedia.com"
        when (brand) {
            "airAsiaGo" -> {
                setPOS(PointOfSaleId.AIRASIAGO_THAILAND)
                assertEquals(urlWithoutWWW, featureConfig.touchupE3EndpointUrlIfRequired(wwwUrl))
            }
            else -> assertEquals(wwwUrl, featureConfig.touchupE3EndpointUrlIfRequired(wwwUrl))
        }
    }

    @Test
    fun testIsGoogleSignInEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "cheapTickets" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "ebookers" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "expedia" -> assertTrue(featureConfig.isGoogleSignInEnabled)
            "lastMinute" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "mrJet" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "orbitz" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "travelocity" -> assertFalse(featureConfig.isGoogleSignInEnabled)
            "wotif" -> assertFalse(featureConfig.isGoogleSignInEnabled)
        }
    }

    @Test
    fun testIsFacebookLoginIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookLoginIntegrationEnabled)
            else -> assertTrue(featureConfig.isFacebookLoginIntegrationEnabled)
        }
    }

    @Test
    fun testIsFacebookShareIntegrationEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isFacebookShareIntegrationEnabled)
            else -> assertTrue(featureConfig.isFacebookShareIntegrationEnabled)
        }
    }

    @Test
    fun testIsAppIntroEnabled() {
        when (brand) {
            "expedia" -> assertTrue(featureConfig.isAppIntroEnabled)
            else -> assertFalse(featureConfig.isAppIntroEnabled)
        }
    }

    @Test
    fun testGetLaunchScreenActionLogo() {
        when (brand) {
            "lastMinute" -> assertNotEquals(0, featureConfig.launchScreenActionLogo)
            "wotif" -> assertNotEquals(0, featureConfig.launchScreenActionLogo)
            else -> assertEquals(0, featureConfig.launchScreenActionLogo)
        }
    }

    @Test
    fun testGetPOSSpecificBrandName() {
        val brandName = BuildConfig.brand
        when (brand) {
            "lastMinute" -> assertNotEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
            else -> assertEquals(brandName, featureConfig.getPOSSpecificBrandName(context))
        }
    }

    @Test
    fun testIsAbacusTestEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isAbacusTestEnabled)
            else -> assertTrue(featureConfig.isAbacusTestEnabled)
        }
    }

    @Test
    fun testGetRewardsLayoutId() {
        when (brand) {
            "cheapTickets" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "ebookers" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "expedia" -> assertEquals(R.layout.pay_with_points_widget_stub, featureConfig.rewardsLayoutId)
            "mrJet" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            "orbitz" -> assertEquals(R.layout.bucks_widget_stub, featureConfig.rewardsLayoutId)
            else -> assertEquals(0, featureConfig.rewardsLayoutId)
        }
    }

    @Test
    fun testIsRewardProgramPointsType() {
        when (brand) {
            "expedia" -> assertTrue(featureConfig.isRewardProgramPointsType)
            else -> assertFalse(featureConfig.isRewardProgramPointsType)
        }
    }

    @Test
    fun testGetRewardTierAPINames() {
        val rewardTierAPINamesOWW = arrayOf("SILVER", "GOLD", "PLATINUM")
        val rewardTierAPINamesBEX = arrayOf("BLUE", "SILVER", "GOLD")
        when (brand) {
            "cheapTickets" -> assertArrayEquals(rewardTierAPINamesOWW, featureConfig.rewardTierAPINames)
            "ebookers" -> assertArrayEquals(rewardTierAPINamesOWW, featureConfig.rewardTierAPINames)
            "expedia" -> assertArrayEquals(rewardTierAPINamesBEX, featureConfig.rewardTierAPINames)
            "mrJet" -> assertArrayEquals(rewardTierAPINamesOWW, featureConfig.rewardTierAPINames)
            "orbitz" -> assertArrayEquals(rewardTierAPINamesOWW, featureConfig.rewardTierAPINames)
            else -> assertNull(featureConfig.rewardTierAPINames)
        }
    }

    @Test
    fun testGetRewardTierSupportNumberConfigNames() {
        val rewardTierSupportPhoneNumberConfigNamesOWW = arrayOf("supportPhoneNumberSilver", "supportPhoneNumberGold", "supportPhoneNumberPlatinum")
        val rewardTierSupportPhoneNumberConfigNamesBEX = arrayOf("supportPhoneNumber", "supportPhoneNumberSilver", "supportPhoneNumberGold")
        when (brand) {
            "cheapTickets" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOWW, featureConfig.rewardTierSupportNumberConfigNames)
            "ebookers" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOWW, featureConfig.rewardTierSupportNumberConfigNames)
            "expedia" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesBEX, featureConfig.rewardTierSupportNumberConfigNames)
            "mrJet" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOWW, featureConfig.rewardTierSupportNumberConfigNames)
            "orbitz" -> assertArrayEquals(rewardTierSupportPhoneNumberConfigNamesOWW, featureConfig.rewardTierSupportNumberConfigNames)
            else -> assertNull(featureConfig.rewardTierSupportNumberConfigNames)
        }
    }

    @Test
    fun testGetRewardTierSupportEmailConfigNames() {
        val rewardTierSupportEmailConfigNamesExpedia = arrayOf(null, "supportEmailSilver", "supportEmailGold")
        when (brand) {
            "expedia" -> assertArrayEquals(rewardTierSupportEmailConfigNamesExpedia, featureConfig.rewardTierSupportEmailConfigNames)
            else -> assertNull(featureConfig.rewardTierSupportEmailConfigNames)
        }
    }

    @Test
    fun testIsCommunicateSectionEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isCommunicateSectionEnabled)
            else -> assertTrue(featureConfig.isCommunicateSectionEnabled)
        }
    }

    @Test
    fun testGetUSPointOfSaleId() {
        when (brand) {
            "cheapTickets" -> assertEquals(PointOfSaleId.CHEAPTICKETS, featureConfig.usPointOfSaleId)
            "expedia" -> assertEquals(PointOfSaleId.UNITED_STATES, featureConfig.usPointOfSaleId)
            "orbitz" -> assertEquals(PointOfSaleId.ORBITZ, featureConfig.usPointOfSaleId)
            "travelocity" -> assertEquals(PointOfSaleId.TRAVELOCITY, featureConfig.usPointOfSaleId)
            else -> assertNull(featureConfig.usPointOfSaleId)
        }
    }

    @Test
    fun testIsGoogleAccountChangeEnabled() {
        when (brand) {
            "cheapTickets" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "ebookers" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "mrJet" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            "orbitz" -> assertTrue(featureConfig.isGoogleAccountChangeEnabled)
            else -> assertFalse(featureConfig.isGoogleAccountChangeEnabled)
        }
    }

    @Test
    fun testGetOmnitureEventValue() {
        val rewardNameKey = OmnitureTracking.OmnitureEventName.REWARD_PROGRAM_NAME
        when (brand) {
            "cheapTickets" -> assertEquals("cheaptickets", featureConfig.getOmnitureEventValue(rewardNameKey))
            "ebookers" -> assertEquals("ebookers", featureConfig.getOmnitureEventValue(rewardNameKey))
            "expedia" -> assertEquals("expedia", featureConfig.getOmnitureEventValue(rewardNameKey))
            "mrJet" -> assertEquals("mrjet", featureConfig.getOmnitureEventValue(rewardNameKey))
            "orbitz" -> assertEquals("orbitz", featureConfig.getOmnitureEventValue(rewardNameKey))
            else -> assertNull(featureConfig.getOmnitureEventValue(rewardNameKey))
        }
    }

    @Test
    fun testShouldShowMemberTier() {
        when (brand) {
            "cheapTickets" -> assertFalse(featureConfig.shouldShowMemberTier())
            else -> assertTrue(featureConfig.shouldShowMemberTier())
        }
    }

    @Test
    fun testGetSharableFallbackImageURL() {
        when (brand) {
            "expedia" -> assertEquals("http://images.trvl-media.com/mobiata/fb/exp-fb-share.png", featureConfig.sharableFallbackImageURL)
            else -> assertNull(featureConfig.sharableFallbackImageURL)
        }
    }

    @Test
    fun testShouldDisplayItinTrackAppLink() {
        when (brand) {
            "cheapTickets" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            "ebookers" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            "mrJet" -> assertFalse(featureConfig.shouldDisplayItinTrackAppLink())
            else -> assertTrue(featureConfig.shouldDisplayItinTrackAppLink())
        }
    }

    @Test
    fun testShouldSetExistingUserForTune() {
        when (brand) {
            "cheapTickets" -> assertTrue(featureConfig.shouldSetExistingUserForTune())
            "orbitz" -> assertTrue(featureConfig.shouldSetExistingUserForTune())
            else -> assertFalse(featureConfig.shouldSetExistingUserForTune())
        }
    }

    @Test
    fun testShouldShowItinShare() {
        when (brand) {
            "cheapTickets" -> assertFalse(featureConfig.shouldShowItinShare())
            "ebookers" -> assertFalse(featureConfig.shouldShowItinShare())
            "mrJet" -> assertFalse(featureConfig.shouldShowItinShare())
            else -> assertTrue(featureConfig.shouldShowItinShare())
        }
    }

    @Test
    fun testIsRateOurAppEnabled() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.isRateOurAppEnabled)
            else -> assertTrue(featureConfig.isRateOurAppEnabled)
        }
    }

    @Test
    fun testIsRewardsCardEnabled() {
        when (brand) {
            "orbitz" -> assertTrue(featureConfig.isRewardsCardEnabled)
            else -> assertFalse(featureConfig.isRewardsCardEnabled)
        }
    }

    @Test
    fun testShowUserRewardsEnrollmentCheck() {
        when (brand) {
            "cheapTickets" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "ebookers" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "expedia" -> assertEquals(PointOfSale.getPointOfSale().shouldShowRewards(), featureConfig.showUserRewardsEnrollmentCheck())
            "mrJet" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            "orbitz" -> assertTrue(featureConfig.showUserRewardsEnrollmentCheck())
            else -> assertFalse(featureConfig.showUserRewardsEnrollmentCheck())
        }
    }

    @Test
    fun testShowJoinRewardsCard() {
        when (brand) {
            "orbitz" -> assertTrue(PointOfSale.getPointOfSale().shouldShowJoinRewardsCard())
            else -> assertFalse(PointOfSale.getPointOfSale().shouldShowJoinRewardsCard())
        }
    }

    @Test
    fun testSendEapidToTuneTracking() {
        when (brand) {
            "lastMinute" -> assertTrue(featureConfig.sendEapidToTuneTracking())
            else -> assertFalse(featureConfig.sendEapidToTuneTracking())
        }
    }

    @Test
    fun testShouldShowPackageIncludesView() {
        when (brand) {
            "ebookers" -> assertFalse(featureConfig.shouldShowPackageIncludesView())
            else -> assertTrue(featureConfig.shouldShowPackageIncludesView())
        }
    }

    @Test
    fun testShowHotelLoyaltyEarnMessage() {
        when (brand) {
            "airAsiaGo" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "lastMinute" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "travelocity" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            "wotif" -> assertFalse(featureConfig.showHotelLoyaltyEarnMessage())
            else -> assertTrue(featureConfig.showHotelLoyaltyEarnMessage())
        }
    }

    @Test
    fun testShouldShowVIPLoyaltyMessage() {
        when (brand) {
            "expedia" -> assertTrue(featureConfig.shouldShowVIPLoyaltyMessage())
            else -> assertFalse(featureConfig.shouldShowVIPLoyaltyMessage())
        }
    }

    @Test
    fun testIsFirebaseEnabled() {
        // NOTE: leaving this for now, at the request of product
        assertFalse(featureConfig.isFirebaseEnabled)
    }

    @Test
    fun testIsRecaptchaEnabled() {
        when (brand) {
            "expedia" -> assertTrue(featureConfig.isRecaptchaEnabled)
            else -> assertFalse(featureConfig.isRecaptchaEnabled)
        }
    }

    @Test
    fun testGetCopyrightLogoUrl() {
        val posWebsiteUrl = PointOfSale.getPointOfSale().websiteUrl
        val appCopyrightUrl = context.getString(R.string.app_copyright_logo_url)
        when (brand) {
            "expedia" -> assertEquals(appCopyrightUrl, featureConfig.getCopyrightLogoUrl(context))
            else -> assertEquals(posWebsiteUrl, featureConfig.getCopyrightLogoUrl(context))
        }
    }

    @Test
    fun testGetPOSSpecificBrandLogo() {
        when (brand) {
            "lastMinute" -> {
                setPOS(PointOfSaleId.LASTMINUTE)
                assertNotEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
            }
            else -> assertEquals(R.drawable.app_copyright_logo, featureConfig.posSpecificBrandLogo)
        }
    }

    @Test
    fun testGetRewardsCardUrl() {
        when (brand) {
            "orbitz" -> assertEquals("http://www.orbitz.com/rewards/visacard", featureConfig.getRewardsCardUrl(context))
            else -> assertNull(featureConfig.getRewardsCardUrl(context))
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
            "wotif" -> assertEquals("21/02", formattedDateTime)
        }
    }
}
