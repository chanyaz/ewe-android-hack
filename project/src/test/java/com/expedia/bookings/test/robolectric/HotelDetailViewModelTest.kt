package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
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
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.expedia.vm.packages.PackageHotelDetailViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertEquals

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

    @Test fun strikeThroughPriceShouldShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val df = DecimalFormat("#")
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers + 10f
        vm.hotelOffersSubject.onNext(offer1)
        assertEquals("$" + df.format(chargeableRateInfo.strikethroughPriceToShowUsers), vm.strikeThroughPriceObservable.value)
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

        assertEquals("Regularly ${vm.strikeThroughPriceObservable.value}, now ${vm.priceToShowCustomerObservable.value}.\\u0020Original price discounted ${vm.discountPercentageObservable.value.first}.\\u0020",
                testSubscriberText.onNextEvents[0])
    }


    @Test fun getHotelPriceContentDescriptionTestNoStrikeThrough() {
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

        val offer = HotelOffersResponse.convertToHotelOffersResponse(offer1, packageOffer, packageSearchParams)

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

    @Test fun earnMessagePriceIsShownWithDecimalPoints() {
        SettingUtils.save(context, R.string.preference_enable_hotel_loyalty_earn_message, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)
        loyaltyPriceInfo("320.56")
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.earnMessageVisibilityObservable.value)
        assertEquals("Earn $320.56", vm.earnMessageObservable.value.toString())
    }

    @Test fun earnMessagePriceIsShownWithoutDecimalPoints() {
        SettingUtils.save(context, R.string.preference_enable_hotel_loyalty_earn_message, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)
        loyaltyPriceInfo("320")
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.earnMessageVisibilityObservable.value)
        assertEquals("Earn $320", vm.earnMessageObservable.value.toString())
    }

    @Test fun earnMessagePointsIsShown() {
        SettingUtils.save(context, R.string.preference_enable_hotel_loyalty_earn_message, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 1000, 1320), null), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.earnMessageVisibilityObservable.value)
        assertEquals("Earn 1,320 points", vm.earnMessageObservable.value.toString())
    }

    @Test fun earnMessagePointsIsNotShown() {
        SettingUtils.save(context, R.string.preference_enable_hotel_loyalty_earn_message, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)
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
            UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser())
        } else if (User.isLoggedIn(context)) {
            User.signOut(context)
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

    @Test fun TestPromoImageForRoomLeftNotShown() {
        setMemberDeal(false, false)
        offer1.hotelRoomResponse[0].currentAllotment = "2"
        offer1.hotelRoomResponse[0].isSameDayDRR = false
        offer1.hotelRoomResponse[0].isDiscountRestrictedToCurrentSourceType = false
        vm.promoImageObservable.onNext(vm.getPromoImage(offer1.hotelRoomResponse[0]))
        assertEquals(0, vm.promoImageObservable.value)
    }

    @Test fun TestPromoImageForAllDealsNotShown() {
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

    @Test fun priceShownToCustomerIncludesCustomerFees() {
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

        hotelSoldOutTestSubscriber.assertValues(false, false, true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun regularAndVIPLoyaltyPointsAppliedHeaderVisibility() {

        //Non VIP hotel and one of the hotel room has loyality info (isBurnApplied = true)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        offer1.isVipAccess = false;
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.hasRegularLoyaltyPointsAppliedObservable.value)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)

        //Non VIP hotel and none of the hotel room has loyality info (isBurnApplied = false)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = false
        offer1.isVipAccess = false;
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)

        //VIP hotel and one of the hotel room has loyality info (isBurnApplied = true)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        offer1.isVipAccess = true;
        vm.hotelOffersSubject.onNext(offer1)
        assertTrue(vm.hasVipAccessLoyaltyObservable.value)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)

        //VIP hotel and none of the hotel room has loyality info (isBurnApplied = false)
        offer1.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = false
        offer1.isVipAccess = true;
        vm.hotelOffersSubject.onNext(offer1)
        assertFalse(vm.hasVipAccessLoyaltyObservable.value)
        assertFalse(vm.hasRegularLoyaltyPointsAppliedObservable.value)
    }

    private fun loyaltyPriceInfo(price: String) {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_hotel_earn_messaging_enabled.json")
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val chargeableRateInfo = offer1.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, PriceEarnInfo(Money(price, "USD"), Money("0", "USD"), Money(price, "USD"))), true)
        chargeableRateInfo.loyaltyInfo = loyaltyInfo
    }


    private fun makeHotel(): ArrayList<HotelOffersResponse.HotelRoomResponse> {
        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();

        var hotel = HotelOffersResponse.HotelRoomResponse()
        var valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        var bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        var bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
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
        var checkIn = LocalDate.now().plusDays(2)
        var checkOut = LocalDate.now().plusDays(5)
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
