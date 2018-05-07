package com.expedia.bookings.tracking

import android.content.Context
import android.content.pm.PackageInfo
import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.OmnitureMatchers.Companion.withProductsString
import com.expedia.bookings.test.OmnitureMatchers.Companion.withProps
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.DebugInfoUtils
import com.google.android.gms.common.GoogleApiAvailability
import com.mobiata.android.util.SettingUtils
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class OmnitureTrackingTest {

    private val USER_EMAIL = "testuser@expedia.com"
    private val USER_EMAIL_HASH = "1941c6bff303b2fb1af6801a7eb809e657bc611e8e2d76c44961b90aec193f5a"

    private val context: Context by lazy {
        RuntimeEnvironment.application
    }
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun guidSentInProp23() {
        OmnitureTracking.trackAccountPageLoad()

        val expectedGuid = DebugInfoUtils.getMC1CookieStr(context).replace("GUID=", "")
        assertStateTracked(withProps(mapOf(23 to expectedGuid)), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun emailHashedWithSHA256() {
        givenUserIsSignedIn()

        OmnitureTracking.trackAccountPageLoad()

        assertStateTracked(withProps(mapOf(11 to USER_EMAIL_HASH)), mockAnalyticsProvider)
    }

    @Test
    fun playServicesVersionIs0WhenNotInstalled() {
        OmnitureTracking.trackAccountPageLoad()

        assertStateTracked(withProps(mapOf(27 to "0")), mockAnalyticsProvider)
    }

    @Test
    fun playServicesVersionIsValidWhenInstalled() {
        val gmsPackageInfo = PackageInfo()
        gmsPackageInfo.packageName = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE
        gmsPackageInfo.versionCode = 11000001
        val shadowPackageManager = shadowOf(RuntimeEnvironment.application.packageManager)
        shadowPackageManager.addPackage(gmsPackageInfo)

        OmnitureTracking.trackAccountPageLoad()

        assertStateTracked(withProps(mapOf(27 to "11000001")), mockAnalyticsProvider)
    }

    @Test
    fun dateFormatTimeZoneDSTCrashFixed() {
        val originalDefaultTimeZone = DateTimeZone.getDefault()
        DateTimeZone.setDefault(DateTimeZone.forID("America/Sao_Paulo"))

        val hotelOffersResponse = HotelOffersResponse()
        hotelOffersResponse.checkInDate = "2017-10-15"
        hotelOffersResponse.checkOutDate = "2017-10-16"

        OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse, false, false, false, false, PageUsableData(), false)

        assertStateTracked(
                "App.Hotels.Infosite",
                allOf(
                        withProps(mapOf(5 to "2017-10-15", 6 to "2017-10-16")),
                        withEvars(mapOf(6 to "1"))),
                mockAnalyticsProvider)

        DateTimeZone.setDefault(originalDefaultTimeZone)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun remoteEnabledAbacusTestIsTracked() {
        val abTest = ABTest(12345, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest)

        val s = AppAnalytics()
        OmnitureTracking.trackAbacusTest(s, abTest)

        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertTrue(evar!!.contains("12345"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun remoteDisabledAbacusTestIsNotTracked() {
        val abTest = ABTest(12345, true)

        val s = AppAnalytics()
        OmnitureTracking.trackAbacusTest(s, abTest)
        assertNull(s.getEvar(34))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun remoteDisabledAbacusTestIsTrackedWithOverride() {
        val abTest = ABTest(12345, true)
        SettingUtils.save(context, abTest.key.toString(), AbacusVariant.BUCKETED.value)

        val s = AppAnalytics()
        OmnitureTracking.trackAbacusTest(s, abTest)
        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertTrue(evar!!.contains("12345"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackedWithNoABResponse() {
        val abTest = ABTest(12345)

        val s = AppAnalytics()
        OmnitureTracking.trackAbacusTest(s, abTest)
        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertEquals("12345.0.-1", evar)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMappingForAllPageEvents() {
        // This checks that there are an equal number of "event" mappings for
        val allEvents = enumValues<OmnitureTracking.PageEvent>().toMutableList()

        val eventString = OmnitureTracking.getEventStringFromEventList(allEvents)
        val eventsInStringCount = eventString.split(",").size

        assertEquals(allEvents.count(), eventsInStringCount)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun launchScreenTilesTracked() {
        val testEvents = listOf(
                OmnitureTracking.PageEvent.LAUNCHSCREEN_LOB_BUTTONS,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_ACTIVE_ITINERARY,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_AIR_ATTACH,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_MEMBER_DEALS_CARD,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_HOTELS_NEARBY,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_SIGN_IN_CARD,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_GLOBAL_NAV,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_LMD,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_MESO_HOTEL_A2A_B2P,
                OmnitureTracking.PageEvent.LAUNCHSCREEN_MESO_DESTINATION
        )
        OmnitureTracking.trackPageLoadLaunchScreen(testEvents)
        assertStateTracked("App.LaunchScreen", withEventsString("event321,event322,event323,event324,event326,event327,event328,event329,event336,event337"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForPackagesBackFlowABTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.PackagesBackFlowFromOverview)
        OmnitureTracking.trackPackagesBundlePageLoad(getPackageDetails().pricing.packageTotal.amount.toDouble(), null)
        assertStateTracked(withProps(mapOf(34 to "16163.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForPackagesBackFlowABTestControlled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.PackagesBackFlowFromOverview)
        OmnitureTracking.trackPackagesBundlePageLoad(getPackageDetails().pricing.packageTotal.amount.toDouble(), null)
        assertStateTracked(withProps(mapOf(34 to "16163.0.0")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackConfirmationViewItinClick() {
        OmnitureTracking.trackConfirmationViewItinClick()
        val controlEvar = mapOf(28 to "App.CKO.Confirm.ViewItinerary")
        OmnitureTestUtils.assertLinkTracked("Confirmation Trip Action", "App.CKO.Confirm.ViewItinerary", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOmnitureTrackingFlightPaymentFeesClick() {
        OmnitureTracking.trackFlightPaymentFeesClick()

        OmnitureTestUtils.assertLinkTracked("App.Flight.Search.PaymentFee", "App.Flight.Search.PaymentFee", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(16 to "App.Flight.Search.PaymentFee")),
                OmnitureMatchers.withEvars(mapOf(28 to "App.Flight.Search.PaymentFee"))), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testGalleryGridView() {
        val pageUsable = PageUsableData().apply {
            markPageLoadStarted(1000)
            markAllViewsLoaded(2000)
        }
        OmnitureTracking.trackHotelDetailGalleryGridView(7, pageUsable, false, 8, "123")
        assertStateTracked(
                "App.Hotels.Infosite.Gallery",
                Matchers.allOf(
                        withEventsString("event357=7,event363=8,event220,event221=1.00"),
                        withProductsString(";Hotel:123;;"),
                        withProps(mapOf(2 to "hotels")),
                        withEvars(mapOf(2 to "D=c2"))),
                mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testTrackFlightsCardExpiryABTest() {
        val abTest = ABTest(24734, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest)
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.FLIGHTS_V2)
        assertStateTracked(withProps(mapOf(34 to "15457.0.-1|24734.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontTrackFlightsCardExpiryABTest() {
        val abTest = ABTest(24734, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest, 0)
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.FLIGHTS_V2)
        assertStateTracked(withProps(mapOf(34 to "15457.0.-1|24734.0.0")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testTrackPackageCardExpiryABTest() {
        val abTest = ABTest(24734, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest)
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.PACKAGES)
        assertStateTracked(withProps(mapOf(34 to "24734.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontTrackPackageCardExpiryABTest() {
        val abTest = ABTest(24734, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest, 0)
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.PACKAGES)
        assertStateTracked(withProps(mapOf(34 to "24734.0.0")), mockAnalyticsProvider)
    }

    @Test
    fun testCustomerFirstGuaranteeLaunchCardClick() {
        OmnitureTracking.trackCustomerFirstAccountLinkClick()
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Account.Support.CFG", mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstTwitterClick() {
        OmnitureTracking.trackCustomerFirstTwitterClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Twitter")
        val expectedProp = mapOf(16 to "App.Support.CFG.Twitter")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstMessengerClick() {
        OmnitureTracking.trackCustomerFirstMessengerClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Messenger")
        val expectedProp = mapOf(16 to "App.Support.CFG.Messenger")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstPhoneClick() {
        OmnitureTracking.trackCustomerFirstPhoneClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Phone")
        val expectedProp = mapOf(16 to "App.Support.CFG.Phone")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Phone", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Phone", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstHelpTopicsClick() {
        OmnitureTracking.trackCustomerFirstHelpTopicsClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.HelpTopics")
        val expectedProp = mapOf(16 to "App.Support.CFG.HelpTopics")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.HelpTopics", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.HelpTopics", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstTwitterDownloadClick() {
        OmnitureTracking.trackCustomerFirstTwitterDownloadClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Twitter.Download")
        val expectedProp = mapOf(16 to "App.Support.CFG.Twitter.Download")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Download", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Download", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstTwitterDownloadCancelClick() {
        OmnitureTracking.trackCustomerFirstTwitterDownloadCancelClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Twitter.Download.Cancel")
        val expectedProp = mapOf(16 to "App.Support.CFG.Twitter.Download.Cancel")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Download.Cancel", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Download.Cancel", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstTwitterOpenClick() {
        OmnitureTracking.trackCustomerFirstTwitterOpenClick()

        val expectedProp = mapOf(16 to "App.Support.CFG.Twitter.Open")
        val expectedEvar = mapOf(28 to "App.Support.CFG.Twitter.Open")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Open", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Open", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstTwitterOpenCancelClick() {
        OmnitureTracking.trackCustomerFirstTwitterOpenCancelClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Twitter.Open.Cancel")
        val expectedProp = mapOf(16 to "App.Support.CFG.Twitter.Open.Cancel")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Open.Cancel", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Twitter.Open.Cancel", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstMessengerDownloadClick() {
        OmnitureTracking.trackCustomerFirstMessengerDownloadClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Messenger.Download")
        val expectedProp = mapOf(16 to "App.Support.CFG.Messenger.Download")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Download", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Download", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstMessengerDownloadCancelClick() {
        OmnitureTracking.trackCustomerFirstMessengerDownloadCancelClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Messenger.Download.Cancel")
        val expectedProp = mapOf(16 to "App.Support.CFG.Messenger.Download.Cancel")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Download.Cancel", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Download.Cancel", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstMessengerOpenClick() {
        OmnitureTracking.trackCustomerFirstMessengerOpenClick()

        val expectedProp = mapOf(16 to "App.Support.CFG.Messenger.Open")
        val expectedEvar = mapOf(28 to "App.Support.CFG.Messenger.Open")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Open", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Open", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun testTrackCustomerFirstMessengerOpenCancelClick() {
        OmnitureTracking.trackCustomerFirstMessengerOpenCancelClick()

        val expectedEvar = mapOf(28 to "App.Support.CFG.Messenger.Open.Cancel")
        val expectedProp = mapOf(16 to "App.Support.CFG.Messenger.Open.Cancel")
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Open.Cancel", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Accounts", "App.Support.CFG.Messenger.Open.Cancel", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    private fun getPackageDetails(): PackageCreateTripResponse.PackageDetails {
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams(destinationCityName = "<B>New</B> <B>York</B>, NY, United States <ap>(JFK-John F. Kennedy Intl.)</ap>",
                childCount = emptyList()))
        Db.setPackageSelectedOutboundFlight(PackageTestUtil.getPackageSelectedOutboundFlight())
        PackageTestUtil.setDbPackageSelectedHotel()
        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.pricing = PackageCreateTripResponse.Pricing()
        packageDetails.pricing.packageTotal = Money(950, "USD")
        return packageDetails
    }

    private fun givenUserIsSignedIn() {
        val user = UserLoginTestUtil.mockUser()
        user.primaryTraveler.email = USER_EMAIL
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }
}
