package com.expedia.bookings.test.robolectric

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
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
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
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
    private var offer3: HotelOffersResponse by Delegates.notNull()

    private val expectedTotalPriceWithMandatoryFees = 42f
    private var context: Context by Delegates.notNull()

    @Before fun before() {
        context = RuntimeEnvironment.application
        vm = HotelDetailViewModel(RuntimeEnvironment.application)

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

        offer3 = HotelOffersResponse()
        offer1.hotelId = "hotel3"
        offer3.hotelName = "hotel3"
        offer1.hotelCity = "hotel3"
        offer1.hotelStateProvince = "hotel3"
        offer1.hotelCountry = "USA"
        offer3.latitude = 101.0
        offer3.longitude = 152.0
        offer3.hotelRoomResponse = emptyList()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityDiscountOnly() {
        val testSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = true, vip = false, promoMessage = "",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.onNextEvents.last())

        triggerHotelMessageContainer(showDiscount = true, vip = false, promoMessage = "",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.onNextEvents.last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityVIPOnly() {
        val testSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = true, promoMessage = "",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.onNextEvents.last())

        triggerHotelMessageContainer(showDiscount = false, vip = true, promoMessage = "",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.onNextEvents.last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityLoyaltyOnly() {
        val testSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "",
                soldOut = false, loyaltyApplied = true, airAttach = false)
        assertTrue(testSubscriber.onNextEvents.last())

        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "",
                soldOut = true, loyaltyApplied = true, airAttach = false)
        assertFalse(testSubscriber.onNextEvents.last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMessageVisibilityPromoOnly() {
        val testSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelMessagingContainerVisibility.subscribe(testSubscriber)
        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "Mobile Exclusive",
                soldOut = false, loyaltyApplied = false, airAttach = false)
        assertTrue(testSubscriber.onNextEvents.last())

        triggerHotelMessageContainer(showDiscount = false, vip = false, promoMessage = "Mobile Exclusive",
                soldOut = true, loyaltyApplied = false, airAttach = false)
        assertFalse(testSubscriber.onNextEvents.last(), "FAILURE: Expected false when soldOut = true")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRatingContainerBackground() {
        val testSubscriber = TestSubscriber.create<Drawable>()
        vm.ratingContainerBackground.subscribe(testSubscriber)

        vm.isUserRatingAvailableObservable.onNext(true)
        assertEquals(ContextCompat.getDrawable(context, R.drawable.gray_background_ripple), testSubscriber.onNextEvents[0],
                "FAILURE: Rating available needs a clickable ripple background")

        vm.isUserRatingAvailableObservable.onNext(false)
        assertEquals(ContextCompat.getDrawable(context, R.color.gray1), testSubscriber.onNextEvents[1],
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
        val testSubscriberText = TestSubscriber<CharSequence>()
        val chargeableRateInfo = offer2.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.averageRate = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers + 10f
        vm.hotelPriceContentDesc.subscribe(testSubscriberText)
        vm.hotelOffersSubject.onNext(offer2)

        assertEquals("Regularly ${vm.strikeThroughPriceObservable.value}, now ${vm.priceToShowCustomerObservable.value}.\u0020Original price discounted ${vm.discountPercentageObservable.value.first}.\u0020",
                testSubscriberText.onNextEvents[0])
    }


    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun getHotelPriceContentDescriptionTestNoStrikeThrough() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val testSubscriberText = TestSubscriber<CharSequence>()
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers - 10f
        chargeableRateInfo.averageRate = 110f
        vm.hotelPriceContentDesc.subscribe(testSubscriberText)
        vm.hotelOffersSubject.onNext(offer1)

        assertEquals("$110/night", testSubscriberText.onNextEvents[0])
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
        val testSubscriber = TestSubscriber<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        testSubscriber.requestMore(100)
        assertEquals("20.00 USD", testSubscriber.onNextEvents[1])
        assertEquals("total fee", context.getString(vm.getFeeTypeText()))
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun resortFeeShowUSPOS() {
        CurrencyUtils.initMap(RuntimeEnvironment.application)
        setPOS(PointOfSaleId.UNITED_STATES)
        val testSubscriber = TestSubscriber<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        testSubscriber.requestMore(100)
        assertEquals("$20", testSubscriber.onNextEvents[1])
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.SAMSUNG))
    fun testPromoImageForMobileExclusiveShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = true
        offer1.hotelRoomResponse[0].currentAllotment = "8"
        offer1.hotelRoomResponse[0].isSameDayDRR = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable, vm.promoImageObservable.value)
    }

    @Test fun testPromoImageForMemberDealNotShown() {
        setMemberDeal(true, true)
        offer1.hotelRoomResponse[0].currentAllotment = "8"
        offer1.hotelRoomResponse[0].isSameDayDRR = false
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test fun testPromoImageForRoomLeftNotShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].currentAllotment = "2"
        offer1.hotelRoomResponse[0].isSameDayDRR = false
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test fun testPromoImageForAllDealsNotShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].currentAllotment = "2"
        offer1.hotelRoomResponse[0].isSameDayDRR = true
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = true
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test fun testPromoImageForTonightOnlyNotShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].isSameDayDRR = true
        offer1.hotelRoomResponse[0].currentAllotment = "10"
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test fun testPromoImageForNoDealsNotShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].currentAllotment = "8"
        offer1.hotelRoomResponse[0].isSameDayDRR = false
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun priceShownToCustomerIncludesCustomerFees() {
        vm.hotelOffersSubject.onNext(offer2)
        val df = DecimalFormat("#")
        val expectedPrice = "$" + df.format(expectedTotalPriceWithMandatoryFees)
        assertEquals(expectedPrice, vm.totalPriceObservable.value)
    }

    @Test fun reviewsClicking() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.reviewsClickedWithHotelData
                .map { hotel -> hotel.hotelName }
                .take(expected.size)
                .subscribe(testSub)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)
        vm.reviewsClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }

    //  TODO: Fix the test.
    //      @Test fun expandNextAvailableRoomOnSoldOut() {
    //        vm.hotelOffersSubject.onNext(offer1)
    //
    //        val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>()
    //        (1..3).forEach {
    //            hotelRoomRateViewModels.add(HotelRoomRateViewModel(RuntimeEnvironment.application, offer1.hotelId, offer1.hotelRoomResponse.first(), "", it, PublishSubject.create(), endlessObserver { }))
    //        }
    //        vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
    //
    //        val expandRoom0TestSubscriber = TestSubscriber.create<Boolean>()
    //        hotelRoomRateViewModels.get(0).expandRoomObservable.subscribe(expandRoom0TestSubscriber)
    //        val expandRoom1TestSubscriber = TestSubscriber.create<Boolean>()
    //        hotelRoomRateViewModels.get(1).expandRoomObservable.subscribe(expandRoom1TestSubscriber)
    //        val expandRoom2TestSubscriber = TestSubscriber.create<Boolean>()
    //        hotelRoomRateViewModels.get(2).expandRoomObservable.subscribe(expandRoom2TestSubscriber)
    //
    //        vm.rowExpandingObservable.onNext(0)
    //        vm.selectedRoomSoldOut.onNext(Unit)
    //
    //        vm.rowExpandingObservable.onNext(1)
    //        vm.selectedRoomSoldOut.onNext(Unit)
    //
    //        expandRoom0TestSubscriber.assertNoValues()
    //        expandRoom1TestSubscriber.assertValues(false)
    //        expandRoom2TestSubscriber.assertValues(false)
    //    }

    @Test fun allRoomsSoldOutSignal() {
        vm.hotelOffersSubject.onNext(offer1)

        val hotelSoldOutTestSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>()
        (1..20).forEach {
            hotelRoomRateViewModels.add(HotelRoomRateViewModel(RuntimeEnvironment.application, offer1.hotelId, offer1.hotelRoomResponse.first(), "", it, PublishSubject.create(), false, LineOfBusiness.HOTELS))
        }
        vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)

        hotelRoomRateViewModels.forEach {
            it.roomSoldOut.onNext(true)
        }

        hotelSoldOutTestSubscriber.assertValues(false, false, true)
    }

    @Test fun hotelSoldOutSignal() {
        val hotelSoldOutTestSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        vm.hotelOffersSubject.onNext(offer3)

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
    fun groupAndSortRoom() {
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

    private fun createRoomResponseList() : List<HotelOffersResponse.HotelRoomResponse> {
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

    private fun createRoomResponse(roomTypeCode: String, priceToShowUser: Float) : HotelOffersResponse.HotelRoomResponse {
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

}
