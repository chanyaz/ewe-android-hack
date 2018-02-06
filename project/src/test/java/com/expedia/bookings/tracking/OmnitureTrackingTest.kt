package com.expedia.bookings.tracking

import android.content.Context
import android.content.pm.PackageInfo
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
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

        val s = ADMS_Measurement()
        OmnitureTracking.trackAbacusTest(s, abTest)

        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertTrue(evar!!.contains("12345"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun remoteDisabledAbacusTestIsNotTracked() {
        val abTest = ABTest(12345, true)

        val s = ADMS_Measurement()
        OmnitureTracking.trackAbacusTest(s, abTest)
        assertNull(s.getEvar(34))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun remoteDisabledAbacusTestIsTrackedWithOverride() {
        val abTest = ABTest(12345, true)
        SettingUtils.save(context, abTest.key.toString(), AbacusVariant.BUCKETED.value)

        val s = ADMS_Measurement()
        OmnitureTracking.trackAbacusTest(s, abTest)
        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertTrue(evar!!.contains("12345"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackedWithNoABResponse() {
        val abTest = ABTest(12345)

        val s = ADMS_Measurement()
        OmnitureTracking.trackAbacusTest(s, abTest)
        val evar = s.getEvar(34)
        assertNotNull(evar)
        assertEquals("12345.0.-1", evar)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun launchScreenTilesTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackPageLoadLaunchScreen(0, "event322,event326,event327")

        assertStateTracked("App.LaunchScreen", withEventsString("event322,event326,event327"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForSeatingClassABTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesDisplayFlightSeatingClass)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackPackagesFlightRoundTripOutLoad(null)
        assertStateTracked(withProps(mapOf(34 to "16300.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForSeatingClassABTestControlled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesDisplayFlightSeatingClass)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackPackagesFlightRoundTripOutLoad(null)
        assertStateTracked(withProps(mapOf(34 to "16300.0.0")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForPackagesBackFlowABTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.PackagesBackFlowFromOverview)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackPackagesBundlePageLoad(getPackageDetails(), null)
        assertStateTracked(withProps(mapOf(34 to "16163.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoggingForPackagesBackFlowABTestControlled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.PackagesBackFlowFromOverview)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackPackagesBundlePageLoad(getPackageDetails(), null)
        assertStateTracked(withProps(mapOf(34 to "16163.0.0")), mockAnalyticsProvider)
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
