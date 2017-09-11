package com.expedia.bookings.widget.itin

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.RoomUpgradeOffersService
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelItinCardTest {

    val server = MockWebServer()
        @Rule get

    lateinit private var roomUpgradeService: RoomUpgradeOffersService
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()
    val portNumber = server.port
    val url = "http://localhost:$portNumber/api/trips/c65fb5fb-489a-4fa8-a007-715b946d3b04/8066893350319/74f89606-241f-4d08-9294-8c17942333dd/1/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/upgradeOffers"

    lateinit private var sut: HotelItinCard

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun vipLabelText() {
        createSystemUnderTest()
        assertEquals("+VIP", getVipLabelTextView().text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun vipLabelTextVisible() {
        createSystemUnderTest()
        givenGoldMember()
        setTestPointOfSale("MockSharedData/pos_with_vipaccess_enabled.json")
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        sut.bind(itinCardData)
        assertEquals(View.VISIBLE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelPosVipSupportDisabled() {
        createSystemUnderTest()
        givenGoldMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        setTestPointOfSale("MockSharedData/pos_with_vipaccess_disabled.json")
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelToBlueMember() {
        createSystemUnderTest()
        givenBlueMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(true).build()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun dontShowVipLabelForNonVipAccessHotel() {
        createSystemUnderTest()
        givenGoldMember()
        val itinCardData = ItinCardDataHotelBuilder().withVipEnabled(false).build()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getVipLabelTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerVisible() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        itinCardData.property.roomUpgradeOfferType = Property.RoomUpgradeType.HAS_UPGRADE_OFFERS
        sut.bind(itinCardData)

        assertEquals(true, sut.isRoomUpgradable())
        assertEquals(View.VISIBLE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerFeatureOn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        itinCardData.property.roomUpgradeOfferType = Property.RoomUpgradeType.NOT_CALLED_UPGRADE_API
        sut.bind(itinCardData)

        assertEquals(true, sut.isRoomUpgradable())
    }

    @Test
    fun roomUpgradeBannerFeatureOff() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        itinCardData.property.roomUpgradeOfferType = Property.RoomUpgradeType.NOT_CALLED_UPGRADE_API
        sut.bind(itinCardData)

        assertEquals(false, sut.isRoomUpgradable())
    }

    @Test
    fun roomUpgradeBannerGoneNoOffers() {
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        itinCardData.property.roomUpgradeOfferType = Property.RoomUpgradeType.NO_UPGRADE_OFFERS
        sut.bind(itinCardData)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerGoneFeatureOff() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        sut.bind(itinCardData)
        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerGoneForSharedItin() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).isSharedItin(true).build()
        sut.bind(itinCardData)

        assertEquals(false, sut.isRoomUpgradable())
        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeBannerGoneInDetails() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)
        createSystemUnderTest()
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        itinCardData.property.roomUpgradeOfferType = Property.RoomUpgradeType.HAS_UPGRADE_OFFERS
        sut.bind(itinCardData)
        sut.expand(false)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
        assertEquals(View.VISIBLE, getUpgradeButton().visibility)
    }

    @Test
    fun roomFetchOffersObserver() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)
        setupRoomUpgradeService()
        createSystemUnderTest()
        sut.roomUpgradeService = roomUpgradeService
        val testSubscriber = TestSubscriber<Property.RoomUpgradeType>()
        sut.mRoomUpgradeOffersSubject.subscribe(testSubscriber)
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl(url).build()
        sut.bind(itinCardData)

        testSubscriber.requestMore(100L)
        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(Property.RoomUpgradeType.HAS_UPGRADE_OFFERS)

        assertEquals(View.VISIBLE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomFetchOffersErrorObserver() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)
        setupRoomUpgradeService()
        createSystemUnderTest()
        sut.roomUpgradeService = roomUpgradeService

        val testSubscriber = TestSubscriber<Property.RoomUpgradeType>()
        sut.mRoomUpgradeOffersSubject.subscribe(testSubscriber)
        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl("https:://www.notarealurl.com").build()
        sut.bind(itinCardData)

        testSubscriber.requestMore(100L)
        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(Property.RoomUpgradeType.NO_UPGRADE_OFFERS)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    @Test
    fun roomUpgradeUnavailableNoRoomOfferApiLink() {
        createSystemUnderTest()

        val itinCardData = ItinCardDataHotelBuilder().withRoomUpgradeApiUrl("").build()
        sut.bind(itinCardData)

        assertEquals(View.GONE, getUpgradeBannerTextView().visibility)
    }

    private fun setTestPointOfSale(filePath: String) {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, filePath)
    }

    private fun getVipLabelTextView(): TextView {
        val vipLabelTextView = sut.findViewById<View>(R.id.vip_label_text_view) as TextView
        return vipLabelTextView
    }

    private fun getUpgradeBannerTextView(): TextView {
        val upgradeBanner = sut.findViewById<View>(R.id.room_upgrade_available_banner) as TextView
        return upgradeBanner
    }

    private fun getUpgradeButton(): TextView {
        val upgradeBanner = sut.findViewById<View>(R.id.room_upgrade_button) as TextView
        return upgradeBanner
    }

    private fun createSystemUnderTest() {
        activity.setTheme(R.style.NewLaunchTheme)
        val itinCard = HotelItinCard(activity, null)
        LayoutInflater.from(activity).inflate(R.layout.widget_itin_card, itinCard)
        sut = itinCard
    }

    private fun givenBlueMember() {
        createUser(LoyaltyMembershipTier.BASE)
    }

    private fun givenGoldMember() {
        createUser(LoyaltyMembershipTier.TOP)
    }

    private fun createUser(loyaltyMembershipTier: LoyaltyMembershipTier) {
        val goldCustomer = UserLoginTestUtil.mockUser(loyaltyMembershipTier)
        UserLoginTestUtil.setupUserAndMockLogin(goldCustomer)
    }

    private fun setupRoomUpgradeService() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        roomUpgradeService = RoomUpgradeOffersService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }
}
