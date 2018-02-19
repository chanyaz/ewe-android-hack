package com.expedia.bookings.test.robolectric

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.payment.PriceEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.RetrofitError
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomDetailViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.mobiata.android.util.SettingUtils
import io.reactivex.subjects.PublishSubject
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelDetailViewModelTest {

    // TODO: Improve HotelDetailViewModel test coverage
    //  -- TODO: Use MockHotelServiceTestRule (It provides helper functions to grab hotel responses. We shouldn't be creating mock hotel objects (see: makeHotel())

    private var vm: HotelDetailViewModel by Delegates.notNull()
    private var offer1: HotelOffersResponse by Delegates.notNull()
    private var offer2: HotelOffersResponse by Delegates.notNull()
    private var soldOutOffer: HotelOffersResponse by Delegates.notNull()

    private val expectedTotalPriceWithMandatoryFees = 42f
    private var context: Context by Delegates.notNull()

    private val mockHotelInfoManager = TestHotelInfoManager()
    private val mockHotelSearchManager = Mockito.mock(HotelSearchManager::class.java)
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before fun before() {
        context = RuntimeEnvironment.application
        vm = HotelDetailViewModel(context, mockHotelInfoManager, mockHotelSearchManager)

        offer1 = HotelOffersResponse()
        offer1.hotelId = "hotel1"
        offer1.hotelName = "hotel1"
        offer1.hotelCity = "hotel1"
        offer1.hotelStateProvince = "hotel1"
        offer1.hotelCountry = "USA"
        offer1.latitude = 1.0
        offer1.longitude = 2.0
        offer1.hotelRoomResponse = makeHotel()

        offer2 = HotelOffersResponse()
        offer1.hotelId = "hotel2"
        offer2.hotelName = "hotel2"
        offer1.hotelCity = "hotel2"
        offer1.hotelStateProvince = "hotel3"
        offer1.hotelCountry = "USA"
        offer2.latitude = 100.0
        offer2.longitude = 150.0
        offer2.hotelRoomResponse = makeHotel()
        offer2.hotelAmenities = arrayListOf(HotelOffersResponse.HotelAmenities().apply { id = "-1" })

        soldOutOffer = HotelOffersResponse()
        offer1.hotelId = "hotel3"
        soldOutOffer.hotelName = "hotel3"
        offer1.hotelCity = "hotel3"
        offer1.hotelStateProvince = "hotel3"
        offer1.hotelCountry = "USA"
        soldOutOffer.latitude = 101.0
        soldOutOffer.longitude = 152.0
        soldOutOffer.hotelRoomResponse = emptyList()
        soldOutOffer.hotelAmenities = arrayListOf(HotelOffersResponse.HotelAmenities().apply { id = "14" })

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityDiscountOnly() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = true, vip = false, promoMessage = "",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.values().last())

        triggerHotelMessageContainer(showDiscount = true, vip = false, promoMessage = "",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.values().last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityVIPOnly() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = true, promoMessage = "",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.values().last())

        triggerHotelMessageContainer(showDiscount = false, vip = true, promoMessage = "",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.values().last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityLoyaltyOnly() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "",
                soldOut = false, loyaltyApplied = true, airAttach = false)
        assertTrue(testSubscriber.values().last())

        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "",
                soldOut = true, loyaltyApplied = true, airAttach = false)
        assertFalse(testSubscriber.values().last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityPromoOnly() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "Mobile Exclusive",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.values().last())

        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "Mobile Exclusive",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.values().last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRatingContainerBackground() {
        val testSubscriber = TestObserver.create<Drawable>()
        vm.ratingContainerBackground.subscribe(testSubscriber)

        vm.isUserRatingAvailableObservable.onNext(true)
        assertEquals(ContextCompat.getDrawable(context, R.drawable.gray_background_ripple), testSubscriber.values()[0],
                "FAILURE: Rating available needs a clickable ripple background")

        vm.isUserRatingAvailableObservable.onNext(false)
        assertEquals(ContextCompat.getDrawable(context, R.color.gray100), testSubscriber.values()[1],
                "FAILURE: No rating available needs a static background")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun strikeThroughPriceShouldShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val df = DecimalFormat("#")
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers + 10f
        vm.hotelOffersSubject.onNext(offer1)
        assertEquals("$" + df.format(chargeableRateInfo.strikethroughPriceToShowUsers), vm.strikeThroughPriceObservable.value.toString())
    }

    @Test fun strikeThroughPriceLessThanPriceToShowUsersDontShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers - 10f
        vm.hotelOffersSubject.onNext(offer1)
        assertNull(vm.strikeThroughPriceObservable.value)
    }

    @Test fun strikeThroughPriceSameAsPriceToShowUsersDontShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = 0f
        vm.hotelOffersSubject.onNext(offer1)
        assertNull(vm.strikeThroughPriceObservable.value)
    }

    @Test fun getHotelPriceContentDescriptionTestWithStrikeThrough() {
        val testSubscriberText = TestObserver<CharSequence>()
        val chargeableRateInfo = offer2.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.averageRate = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers + 10f
        vm.hotelPriceContentDesc.subscribe(testSubscriberText)
        vm.hotelOffersSubject.onNext(offer2)

        assertEquals("Regularly ${vm.strikeThroughPriceObservable.value}, now ${vm.priceToShowCustomerObservable.value}.\u0020Original price discounted ${vm.discountPercentageObservable.value.first}.\u0020",
                testSubscriberText.values()[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun getHotelPriceContentDescriptionTestNoStrikeThrough() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val testSubscriberText = TestObserver<CharSequence>()
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers - 10f
        chargeableRateInfo.averageRate = 110f
        vm.hotelPriceContentDesc.subscribe(testSubscriberText)
        vm.hotelOffersSubject.onNext(offer1)

        assertEquals("$110/night", testSubscriberText.values()[0])
    }

    @Test
    fun testNullAmenityList() {
        val testAmenitySubscriber = TestObserver<Unit>()
        vm.noAmenityObservable.subscribe(testAmenitySubscriber)
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(testAmenitySubscriber.values().isNotEmpty())
    }

    @Test
    fun testNonExistentAmenity() {
        val testAmenitySubscriber = TestObserver<Unit>()
        vm.noAmenityObservable.subscribe(testAmenitySubscriber)
        vm.hotelOffersSubject.onNext(offer2)
        assertTrue(testAmenitySubscriber.values().isNotEmpty())
    }

    @Test
    fun testPoolAmenity() {
        val testAmenitySubscriber = TestObserver<List<Amenity>>()
        vm.amenitiesListObservable.subscribe(testAmenitySubscriber)
        vm.hotelOffersSubject.onNext(soldOutOffer)
        assertEquals(Amenity.POOL, testAmenitySubscriber.values()[0][0])
    }

    @Test fun discountPercentageShouldNotShowForPackages() {
        val hotelOffer = HotelOffersResponse()
        vm.hotelOffersSubject.onNext(hotelOffer)
        assertFalse(vm.showDiscountPercentageObservable.value)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun resortFeeShowUKPOS() {
        CurrencyUtils.initMap(RuntimeEnvironment.application)
        setPOS(PointOfSaleId.UNITED_KINGDOM)
        val testSubscriber = TestObserver<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        assertEquals("20.00 USD", testSubscriber.values()[1])
        assertEquals("total fee", context.getString(vm.getFeeTypeText()))
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun resortFeeShowUSPOS() {
        CurrencyUtils.initMap(RuntimeEnvironment.application)
        setPOS(PointOfSaleId.UNITED_STATES)
        val testSubscriber = TestObserver<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        assertEquals("$20", testSubscriber.values()[1])
    }

    private fun makeResortFeeResponse(vm: BaseHotelDetailViewModel) {
        offer1.hotelRoomResponse.clear()
        val packageSearchParams = PackageSearchParams.Builder(30, 330)
                .adults(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .destination(SuggestionV4())
                .origin(SuggestionV4())
                .build() as PackageSearchParams

        val packageOffer = PackageOffersResponse()

        val packageHotelOffer = PackageOffersResponse.PackageHotelOffer()
        packageHotelOffer.hotelOffer = makeHotel().first()
        packageHotelOffer.packagePricing = PackageOffersResponse.PackagePricing()
        packageHotelOffer.packagePricing.hotelPricing = PackageOffersResponse.HotelPricing()
        packageHotelOffer.packagePricing.hotelPricing.mandatoryFees = PackageOffersResponse.MandatoryFees()
        packageHotelOffer.packagePricing.hotelPricing.mandatoryFees.feeTotal = Money(20, "USD")
        packageHotelOffer.cancellationPolicy = PackageOffersResponse.CancellationPolicy()
        packageHotelOffer.cancellationPolicy.hasFreeCancellation = false
        packageHotelOffer.pricePerPerson = Money()
        packageHotelOffer.pricePerPerson.amount = BigDecimal(25.00)
        packageHotelOffer.pricePerPerson.currencyCode = "USD"
        packageOffer.packageHotelOffers = arrayListOf(packageHotelOffer)

        val offer = HotelOffersResponse.convertToHotelOffersResponse(offer1, HotelOffersResponse.convertPSSHotelRoomResponse(packageOffer), packageSearchParams.startDate.toString(), packageSearchParams.endDate.toString())

        vm.hotelOffersSubject.onNext(offer)
        vm.addViewsAfterTransition()
    }

    @Test fun discountPercentageShouldNotShowForSWP() {
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.showDiscountPercentageObservable.value)
        assertFalse(vm.showAirAttachSWPImageObservable.value)
    }

    @Test fun zeroDiscountPercentageIsNotShown() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        chargeableRateInfo.discountPercent = 0f
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.showDiscountPercentageObservable.value)
    }

    @Test fun airAttachSWPImageShownForSWP() {
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
        chargeableRateInfo.airAttached = true
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.showDiscountPercentageObservable.value)
        assertTrue(vm.showAirAttachSWPImageObservable.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun earnMessagePriceIsShownWithDecimalPoints() {
        loyaltyPriceInfo("320.56")
        vm.hotelOffersSubject.onNext(offer1)
        if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
            assertTrue(vm.earnMessageVisibilityObservable.value)
            assertEquals("Earn $320.56", vm.earnMessageObservable.value.toString())
        } else {
            assertFalse(vm.earnMessageVisibilityObservable.value)
        }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun earnMessagePriceIsShownWithoutDecimalPoints() {
        loyaltyPriceInfo("320")
        vm.hotelOffersSubject.onNext(offer1)
        if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
            assertTrue(vm.earnMessageVisibilityObservable.value)
            assertEquals("Earn $320", vm.earnMessageObservable.value.toString())
        } else {
            assertFalse(vm.earnMessageVisibilityObservable.value)
        }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun earnMessagePointsIsShown() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 1000, 1320), null), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
        vm.hotelOffersSubject.onNext(offer1)
        if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
            assertTrue(vm.earnMessageVisibilityObservable.value)
            assertEquals("Earn 1,320 points", vm.earnMessageObservable.value.toString())
        } else {
            assertFalse(vm.earnMessageVisibilityObservable.value)
        }
    }

    @Test fun earnMessagePointsIsNotShown() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_hotel_earn_messaging_disabled.json")
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 100, 420), null), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.earnMessageVisibilityObservable.value)
    }

    /**
     * Sets the member deal for the current user
     * @param loginUser : Determines whether the user should get logged in or logged out
     * @param isMemberDeal : Sets the member deal to true or false
     */
    private fun setMemberDeal(loginUser: Boolean, isMemberDeal: Boolean) {
        if (loginUser) {
            UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        }
        offer1.hotelRoomResponse[0].isMemberDeal = isMemberDeal
    }

    @Test fun testMemberDeal() {
        setMemberDeal(true, true)
        assertTrue(vm.hasMemberDeal(offer1.hotelRoomResponse[0]))
    }

    @Test fun testNoMemberDeal() {
        setMemberDeal(true, false)
        assertFalse(vm.hasMemberDeal(offer1.hotelRoomResponse[0]))
    }

    @Test fun testNoUserMemberDeal() {
        setMemberDeal(false, true)
        assertFalse(vm.hasMemberDeal(offer1.hotelRoomResponse[0]))
    }

    @Test fun testNoUserNoMemberDeal() {
        setMemberDeal(false, false)
        assertFalse(vm.hasMemberDeal(offer1.hotelRoomResponse[0]))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun totalPricePullsApiPriceToShowUsers() {
        vm.hotelOffersSubject.onNext(offer2)
        val df = DecimalFormat("#")
        val expectedPrice = "$" + df.format(offer2.hotelRoomResponse[0].rateInfo.chargeableRateInfo.priceToShowUsers)
        assertEquals(expectedPrice, vm.totalPriceObservable.value)
    }

    @Test fun reviewsClicking() {
        val testSub = TestObserver.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.reviewsDataObservable
                .map { hotel -> hotel.hotelName }
                .take(expected.size.toLong())
                .subscribe(testSub)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickObserver.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickObserver.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickObserver.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickObserver.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickObserver.onNext(Unit)
        vm.reviewsClickObserver.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertComplete()
        testSub.assertValueSequence(expected)
    }

    @Test fun allRoomsSoldOutSignal() {
        vm.hotelOffersSubject.onNext(offer1)

        val hotelSoldOutTestSubscriber = TestObserver.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        val hotelRoomDetailViewModels = ArrayList<HotelRoomDetailViewModel>()
        (1..20).forEach {
            hotelRoomDetailViewModels.add(HotelRoomDetailViewModel(RuntimeEnvironment.application,
                    offer1.hotelRoomResponse.first(), offer1.hotelId, 0, 0, true))
        }
        vm.hotelRoomDetailViewModelsObservable.onNext(hotelRoomDetailViewModels)

        hotelRoomDetailViewModels.forEach {
            it.roomSoldOut.onNext(true)
        }

        hotelSoldOutTestSubscriber.assertValues(false, false, true)
    }

    @Test fun hotelSoldOutSignal() {
        val hotelSoldOutTestSubscriber = TestObserver.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        vm.hotelOffersSubject.onNext(soldOutOffer)

        hotelSoldOutTestSubscriber.assertValues(false, true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun regularAndVIPLoyaltyPointsAppliedHeaderVisibility() {

        //Non VIP hotel and one of the hotel room has loyality info (isBurnApplied = true)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        offer1.isVipAccess = false
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.hasRegularLoyaltyPointsAppliedObservable.value)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)

        //Non VIP hotel and none of the hotel room has loyality info (isBurnApplied = false)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = false
        offer1.isVipAccess = false
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)

        //VIP hotel and one of the hotel room has loyality info (isBurnApplied = true)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        offer1.isVipAccess = true
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.hasVipAccessLoyaltyObservable.value)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)

        //VIP hotel and none of the hotel room has loyality info (isBurnApplied = false)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = false
        offer1.isVipAccess = true
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)
    }

    @Test
    fun testGroupAndSortRoom() {
        val roomResponse = createRoomResponseList()
        val sorted = vm.groupAndSortRoomList(roomResponse)
        assertEquals(3, sorted.count())
        assertEquals(sorted["1"]!![0].roomTypeCode, "1")
        assertEquals(sorted["1"]!![1].roomTypeCode, "1")
        assertEquals(sorted["1"]!![2].roomTypeCode, "1")
        assertEquals(sorted["3"]!![0].roomTypeCode, "3")
        assertEquals(sorted["2"]!![0].roomTypeCode, "2")
        assertEquals(sorted["2"]!![1].roomTypeCode, "2")

        assertEquals(sorted["1"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 10.toFloat())
        assertEquals(sorted["1"]!![0].hasFreeCancellation, false)
        assertEquals(sorted["1"]!![1].rateInfo.chargeableRateInfo.priceToShowUsers, 10.toFloat())
        assertEquals(sorted["1"]!![1].hasFreeCancellation, true)
        assertEquals(sorted["1"]!![2].rateInfo.chargeableRateInfo.priceToShowUsers, 1000.toFloat())
        assertEquals(sorted["3"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 15.toFloat())
        assertEquals(sorted["2"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 20.toFloat())
        assertEquals(sorted["2"]!![1].rateInfo.chargeableRateInfo.priceToShowUsers, 100.toFloat())
    }

    @Test
    fun testGroupAndSortNoRoomTypeCode() {
        var roomTypeCode1 = createRoomResponse("typeCode", 20.toFloat())
        var roomTypeCode2 = createRoomResponse("typeCode", 10.toFloat())

        val productKey1 = createRoomResponse(null, 40.toFloat())
        val productKey2 = createRoomResponse(null, 30.toFloat())
        productKey1.productKey = "productKey"
        productKey2.productKey = "productKey"

        val productKey3 = createRoomResponse(null, 60.toFloat())
        val productKey4 = createRoomResponse(null, 50.toFloat())
        productKey3.productKey = "productKey2"
        productKey4.productKey = "productKey2"

        val roomResponse = listOf(productKey2, roomTypeCode2,
                productKey4, roomTypeCode1,
                productKey3, productKey1)

        val sorted = vm.groupAndSortRoomList(roomResponse)

        assertEquals(3, sorted.count())
        assertEquals(sorted["typeCode"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 10.toFloat())
        assertEquals(sorted["typeCode"]!![1].rateInfo.chargeableRateInfo.priceToShowUsers, 20.toFloat())

        assertEquals(sorted["productKey"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 30.toFloat())
        assertEquals(sorted["productKey"]!![1].rateInfo.chargeableRateInfo.priceToShowUsers, 40.toFloat())

        assertEquals(sorted["productKey2"]!![0].rateInfo.chargeableRateInfo.priceToShowUsers, 50.toFloat())
        assertEquals(sorted["productKey2"]!![1].rateInfo.chargeableRateInfo.priceToShowUsers, 60.toFloat())
    }

    @Test
    fun testFetchOffersHappy() {
        val testProgressSub = TestObserver.create<Unit>()
        val testSuccessSub = TestObserver.create<HotelOffersResponse>()

        vm.fetchInProgressSubject.subscribe(testProgressSub)
        vm.hotelOffersSubject.subscribe(testSuccessSub)

        vm.fetchOffers(createSearchParams(), "12345")

        testProgressSub.assertValueCount(1)
        mockHotelInfoManager.offerSuccessSubject.onNext(HotelOffersResponse())
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testFetchOffersDateless() {
        val testDatelessSubscription = TestObserver.create<Boolean>()
        vm.isDatelessObservable.subscribe(testDatelessSubscription)
        mockHotelInfoManager.offerSuccessSubject.onNext(HotelOffersResponse())
        testDatelessSubscription.assertValue(false)
    }

    @Test
    fun testFetchOffersSoldOut() {
        val testSuccessSub = TestObserver.create<HotelOffersResponse>()
        val testFetchOfferSub = TestObserver.create<Unit>()
        val testFetchInfoSub = TestObserver.create<Unit>()

        mockHotelInfoManager.fetchOffersCalled.subscribe(testFetchOfferSub)
        mockHotelInfoManager.fetchInfoCalled.subscribe(testFetchInfoSub)

        vm.hotelOffersSubject.subscribe(testSuccessSub)

        vm.fetchOffers(createSearchParams(), "12345")
        testFetchOfferSub.assertValueCount(1)
        testSuccessSub.assertValueCount(0)

        mockHotelInfoManager.soldOutSubject.onNext(Unit)
        testFetchInfoSub.assertValueCount(1) // After offer fails due to sold out it is expected an offer call is attempted.
        testSuccessSub.assertValueCount(0)

        mockHotelInfoManager.infoSuccessSubject.onNext(HotelOffersResponse())
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testDatesForSoldOut() {
        val testDatesTextSub = TestObserver.create<String>()
        vm.searchInfoObservable.subscribe(testDatesTextSub)
        vm.hotelOffersSubject.onNext(soldOutOffer)

        assertEquals(context.getString(R.string.change_dates), testDatesTextSub.values()[0],
                "Failure: Expected Dates to read Change Dates when hotel sold out!")
    }

    fun testFetchOffersApiError() {
        val testSuccessSub = TestObserver.create<HotelOffersResponse>()
        val testErrorSub = TestObserver.create<ApiError>()
        val testFetchOfferSub = TestObserver.create<Unit>()

        mockHotelInfoManager.fetchOffersCalled.subscribe(testFetchOfferSub)

        vm.hotelOffersSubject.subscribe(testSuccessSub)
        vm.infositeApiErrorSubject.subscribe(testErrorSub)

        vm.fetchOffers(createSearchParams(), "12345")
        testFetchOfferSub.assertValueCount(1)
        testErrorSub.assertValueCount(0)

        mockHotelInfoManager.apiErrorSubject.onNext(ApiError())
        testErrorSub.assertValueCount(1)
        testSuccessSub.assertValueCount(0)
    }

    @Test
    fun testApiErrorObservable() {
        val testSubscriber = TestObserver<ApiError>()
        vm.infositeApiErrorSubject.subscribe(testSubscriber)

        mockHotelInfoManager.apiErrorSubject.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun testInfoRxNetworkErrorTracking() {
        mockHotelInfoManager.infoRetrofitErrorSubject.onNext(RetrofitError.NO_INTERNET)

        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite.Error",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to "NetworkError")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Infosite.Error"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testInfoRxTimeOutErrorTracking() {
        mockHotelInfoManager.infoRetrofitErrorSubject.onNext(RetrofitError.TIMEOUT)

        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite.Error",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to "NetworkTimeOut")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Infosite.Error"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testInfoRxUnknownErrorTracking() {
        mockHotelInfoManager.infoRetrofitErrorSubject.onNext(RetrofitError.UNKNOWN)

        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite.Error",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to "UnknownRetrofitError")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Infosite.Error"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testOfferRxNetworkErrorTracking() {
        mockHotelInfoManager.offerRetrofitErrorSubject.onNext(RetrofitError.NO_INTERNET)

        assertInfoSiteErrorTracking("NetworkError")
    }

    @Test
    fun testOfferRxTimeOutErrorTracking() {
        mockHotelInfoManager.offerRetrofitErrorSubject.onNext(RetrofitError.TIMEOUT)

        assertInfoSiteErrorTracking("NetworkTimeOut")
    }

    @Test
    fun testOfferRxUnknownErrorTracking() {
        mockHotelInfoManager.offerRetrofitErrorSubject.onNext(RetrofitError.UNKNOWN)

        assertInfoSiteErrorTracking("UnknownRetrofitError")
    }

    @Test
    fun testChangeDate() {
        val originalStartDate = LocalDate()
        val originalEndDate = LocalDate(1)

        val testParamsSub = TestObserver.create<HotelSearchParams>()
        val testFetchInProgressSub = TestObserver.create<Unit>()

        vm.hotelId = "test"

        vm.paramsSubject.subscribe(testParamsSub)
        vm.fetchInProgressSubject.subscribe(testFetchInProgressSub)

        val destination = SuggestionV4()
        val originalParams = HotelSearchParams.Builder(0, 0)
                .destination(destination)
                .startDate(originalStartDate)
                .endDate(originalEndDate)
                .adults(1)

        vm.paramsSubject.onNext(originalParams.build() as HotelSearchParams)

        val newStartDate = LocalDate(2)
        val newEndDate = LocalDate().plusDays(4)

        vm.changeDates(newStartDate, newEndDate)

        var newParams = testParamsSub.values()

        assertEquals(2, newParams.count())
        assertEquals(newStartDate, newParams.last().startDate)
        assertEquals(newEndDate, newParams.last().endDate)

        assertEquals(newStartDate, vm.changeDateParams!!.startDate)
        assertEquals(newEndDate, vm.changeDateParams!!.endDate)
        testFetchInProgressSub.assertValueCount(1)
    }

    @Test
    fun testViewDetailsTracking() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_happy_offer.json")
        vm.hotelOffersSubject.onNext(response)
        vm.isDatelessObservable.onNext(false)
        vm.paramsSubject.onNext(createSearchParams())
        vm.trackHotelDetailLoad(false)

        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite",
                Matchers.allOf(OmnitureMatchers.withEventsString("event3")),
                mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA)) // some flavors have Abacus test disabled
    fun testViewDetailsDatelessTracking() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelDatelessInfosite)
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_happy_offer.json")
        vm.hotelOffersSubject.onNext(response)
        vm.isDatelessObservable.onNext(true)
        val params = createSearchParams().apply { isDatelessSearch = true }
        vm.paramsSubject.onNext(params)
        vm.trackHotelDetailLoad(false)

        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite",
                Matchers.allOf(OmnitureMatchers.withEventsString("event3,event11"),
                        OmnitureMatchers.withProductsString("Hotel; Hotel:795934"),
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 4 to "6056742")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 4 to "D=c4", 34 to "24648.0.1"))
                ),
                mockAnalyticsProvider)
    }

    private fun loadOfferInfo(resourcePath: String): HotelOffersResponse {
        return JSONResourceReader(resourcePath).constructUsingGson(HotelOffersResponse::class.java)
    }

    private fun createRoomResponseList(): List<HotelOffersResponse.HotelRoomResponse> {
        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>()

        rooms.add(createRoomResponse("2", 20.toFloat()))
        rooms.add(createRoomResponse("1", 10.toFloat()))
        rooms.last().hasFreeCancellation = true
        rooms.add(createRoomResponse("3", 15.toFloat()))
        rooms.add(createRoomResponse("1", 1000.toFloat()))
        rooms.add(createRoomResponse("2", 100.toFloat()))
        rooms.add(createRoomResponse("1", 10.toFloat()))
        rooms.last().hasFreeCancellation = false

        return rooms
    }

    private fun triggerHotelMessageContainer(showDiscount: Boolean, vip: Boolean, promoMessage: String,
                                             soldOut: Boolean, loyaltyApplied: Boolean, airAttach: Boolean) {
        vm.showDiscountPercentageObservable.onNext(showDiscount)
        vm.hasVipAccessObservable.onNext(vip)
        vm.promoMessageObservable.onNext(promoMessage)
        vm.hotelSoldOut.onNext(soldOut)
        vm.hasRegularLoyaltyPointsAppliedObservable.onNext(loyaltyApplied)
        vm.showAirAttachSWPImageObservable.onNext(airAttach)
    }

    private fun createRoomResponse(roomTypeCode: String?, priceToShowUser: Float): HotelOffersResponse.HotelRoomResponse {
        val room = HotelOffersResponse.HotelRoomResponse()
        room.roomTypeCode = roomTypeCode

        val lowRateInfo = HotelRate()

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo

        rateInfo.chargeableRateInfo.priceToShowUsers = priceToShowUser
        room.rateInfo = rateInfo

        return room
    }

    private fun loyaltyPriceInfo(price: String) {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, PriceEarnInfo(Money(price, "USD"), Money("0", "USD"), Money(price, "USD"))), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
    }

    private fun makeHotel(): ArrayList<HotelOffersResponse.HotelRoomResponse> {
        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>()

        val hotel = HotelOffersResponse.HotelRoomResponse()
        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        val valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        val bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        val bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees = expectedTotalPriceWithMandatoryFees
        hotel.rateInfo = rateInfo

        rooms.add(hotel)
        return rooms
    }

    private fun createSearchParams(): HotelSearchParams {
        val suggestionV4 = SuggestionV4()
        suggestionV4.gaiaId = "1234"
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        suggestionV4.regionNames = regionNames
        val childList = ArrayList<Int>()
        childList.add(1)
        val checkIn = LocalDate.now().plusDays(2)
        val checkOut = LocalDate.now().plusDays(5)
        val numAdults = 2
        return HotelSearchParams.Builder(0, 0)
                .destination(suggestionV4)
                .startDate(checkIn)
                .endDate(checkOut)
                .adults(numAdults)
                .children(childList).build() as HotelSearchParams
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(RuntimeEnvironment.application, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(RuntimeEnvironment.application)
    }

    private fun assertInfoSiteErrorTracking(errorMessage: String) {
        OmnitureTestUtils.assertStateTracked("App.Hotels.Infosite.Error",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to errorMessage)),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Infosite.Error"))),
                mockAnalyticsProvider)
    }

    private class TestHotelInfoManager :
            HotelInfoManager(Mockito.mock(HotelServices::class.java)) {
        val fetchOffersCalled = PublishSubject.create<Unit>()
        val fetchInfoCalled = PublishSubject.create<Unit>()

        override fun fetchOffers(params: HotelSearchParams, hotelId: String) {
            fetchOffersCalled.onNext(Unit)
        }

        override fun fetchInfo(params: HotelSearchParams, hotelId: String) {
            fetchInfoCalled.onNext(Unit)
        }
    }
}
