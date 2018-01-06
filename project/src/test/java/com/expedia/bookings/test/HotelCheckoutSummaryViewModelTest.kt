package com.expedia.bookings.test

import android.app.Application
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.isAllowCheckinCheckoutDatesInlineEnabled
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil

@RunWith(RobolectricRunner::class)
class HotelCheckoutSummaryViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get
    lateinit private var sut: HotelCheckoutSummaryViewModel
    lateinit private var createTripResponse: HotelCreateTripResponse
    lateinit private var hotelProductResponse: HotelCreateTripResponse.HotelProductResponse
    private var createTripResponseObservable = PublishSubject.create<HotelCreateTripResponse>()
    lateinit private var paymentModel: PaymentModel<HotelCreateTripResponse>
    lateinit private var context: Application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun happy() {
        Db.sharedInstance.abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline.key,
                AbacusUtils.DefaultVariant.CONTROL.ordinal)
        givenHappyHotelProductResponse()
        setup()
        val testTextSubscriber = TestSubscriber<String>()
        sut.freeCancellationText.subscribe(testTextSubscriber)

        val checkinFormattedDateSubscriber = TestSubscriber<String>()
        sut.checkinDateFormattedByEEEMMDD.subscribe(checkinFormattedDateSubscriber)

        val testNewDataSubscriber = TestSubscriber<Unit>()
        sut.newDataObservable.subscribe(testNewDataSubscriber)

        createTripResponseObservable.onNext(createTripResponse)
        paymentModel.createTripSubject.onNext(createTripResponse)

        val hotelRoomResponse = hotelProductResponse.hotelRoomResponse
        val rate = hotelRoomResponse.rateInfo.chargeableRateInfo
        val expectedRateAdjustments = rate.getPriceAdjustments()
        val expectedHotelName = hotelProductResponse.getHotelName()
        sut.guestCountObserver.onNext(1)

        assertTrue(sut.isPayLater.value)
        assertFalse(sut.isResortCase.value)
        assertTrue(sut.isPayLaterOrResortCase.value)
        assertEquals(expectedRateAdjustments, sut.priceAdjustments.value)
        assertEquals(expectedHotelName, sut.hotelName.value)
        assertEquals(hotelProductResponse.checkInDate, sut.checkInDate.value)
        assertEquals("Mar 22, 2013 - Mar 23, 2013", sut.checkInOutDatesFormatted.value)
        assertEquals(checkinFormattedDateSubscriber.onNextEvents.size, 0)
        assertEquals(hotelProductResponse.hotelAddress, sut.address.value)
        assertEquals("San Francisco, CA", sut.city.value)
        assertEquals(hotelRoomResponse.roomTypeDescription, sut.roomDescriptions.value)
        assertEquals("1 Night", sut.numNights.value)
        assertEquals("1 guest", sut.numGuests.value)
        assertEquals(hotelRoomResponse.hasFreeCancellation, sut.hasFreeCancellation.value)
        assertEquals(rate.currencyCode, sut.currencyCode.value)
        assertEquals(rate.nightlyRatesPerRoom, sut.nightlyRatesPerRoom.value)
        assertEquals(rate.nightlyRateTotal.toString(), sut.nightlyRateTotal.value)
        assertEquals(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode).formattedMoney, sut.surchargeTotalForEntireStay.value.formattedMoney)
        assertEquals(rate.taxStatusType, sut.taxStatusType.value)
        assertEquals(rate.extraGuestFees, sut.extraGuestFees.value)
        assertEquals(rate.displayTotalPrice.formattedMoney, sut.tripTotalPrice.value)
        assertEquals("$0", sut.dueNowAmount.value)
        assertFalse(sut.showFeesPaidAtHotel.value)
        assertEquals(Money(BigDecimal(rate.totalMandatoryFees.toString()), rate.currencyCode).formattedMoney, sut.feesPaidAtHotel.value)
        assertEquals(testNewDataSubscriber.onNextEvents.size, 1)
        assertEquals("Free cancellation", testTextSubscriber.onNextEvents[0])
        assertNull(sut.burnAmountShownOnHotelCostBreakdown.value)
        assertEquals(hotelRoomResponse.valueAdds, sut.valueAddsListObservable.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun withHappyHotelPropertyFee() {
        givenHappyHotelProductResponseWithPropertyFee()
        setup()

        createTripResponseObservable.onNext(createTripResponse)
        val actualValue = sut.propertyServiceSurcharge.value?.value
        val expectedValue = Money(BigDecimal(7.56), "USD")
        assertEquals(expectedValue.formattedMoney, actualValue?.formattedMoney)
        val surchargeActualValue = sut.surchargeTotalForEntireStay.value
        val surchargeExpectedValue = Money(BigDecimal(329.32), "USD")
        assertEquals(surchargeActualValue.formattedMoney, surchargeExpectedValue.formattedMoney)
    }

    @Test
    fun testToggleOnCheckinCheckoutDatesInline() {
        toggleABTestCheckinCheckoutDatesInline(true)
        assertTrue(isAllowCheckinCheckoutDatesInlineEnabled())
    }

    @Test
    fun testToggleOffCheckinCheckoutDatesInline() {
        toggleABTestCheckinCheckoutDatesInline(false)
        assertFalse(isAllowCheckinCheckoutDatesInlineEnabled())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testDateValuesInHotelCheckoutSummaryWithABTestCheckinCheckoutDatesInlineTurnedOn() {
        toggleABTestCheckinCheckoutDatesInline(true)
        givenHappyHotelProductResponse()
        setup()
        createTripResponseObservable.onNext(createTripResponse)
        val checkinFormattedDateSubscriber = TestSubscriber<String>()
        sut.checkinDateFormattedByEEEMMDD.subscribe(checkinFormattedDateSubscriber)
        val checkoutFormattedDateSubscriber = TestSubscriber<String>()
        sut.checkoutDateFormattedByEEEMMDD.subscribe(checkoutFormattedDateSubscriber)
        toggleABTestCheckinCheckoutDatesInline(true)
        givenHappyHotelProductResponse()
        setup()
        createTripResponseObservable.onNext(createTripResponse)

        assertEquals(1, checkinFormattedDateSubscriber.onNextEvents.size)
        checkinFormattedDateSubscriber.assertValue("Fri, Mar 22")
        assertEquals(1, checkoutFormattedDateSubscriber.onNextEvents.size)
        checkoutFormattedDateSubscriber.assertValue("Sat, Mar 23")
        assertNull(sut.checkInOutDatesFormatted.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testDateValuesInHotelCheckoutSummaryWithABTestCheckinCheckoutDatesInlineTurnedOff() {
        toggleABTestCheckinCheckoutDatesInline(false)
        givenHappyHotelProductResponse()
        setup()
        createTripResponseObservable.onNext(createTripResponse)
        val checkinFormattedDateSubscriber = TestSubscriber<String>()
        sut.checkinDateFormattedByEEEMMDD.subscribe(checkinFormattedDateSubscriber)
        val checkoutFormattedDateSubscriber = TestSubscriber<String>()
        sut.checkoutDateFormattedByEEEMMDD.subscribe(checkoutFormattedDateSubscriber)
        toggleABTestCheckinCheckoutDatesInline(false)
        givenHappyHotelProductResponse()
        setup()
        createTripResponseObservable.onNext(createTripResponse)

        assertEquals(0, checkinFormattedDateSubscriber.onNextEvents.size)
        assertEquals(0, checkoutFormattedDateSubscriber.onNextEvents.size)
        assertEquals("Mar 22, 2013 - Mar 23, 2013", sut.checkInOutDatesFormatted.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun withHappyHotelPropertyFeeWithPOSNotSupported() {
        setPOS(PointOfSaleId.UNITED_KINGDOM)
        givenHappyHotelProductResponseWithPropertyFee()
        setup()

        createTripResponseObservable.onNext(createTripResponse)
        val actualValue = sut.propertyServiceSurcharge.value?.value
        assertNull(actualValue)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun freeCancellationWindowIsProvided() {
        givenHappyHotelProductResponse()
        setup()

        val testTextSubscriber = TestSubscriber<String>()
        val checkInDate = LocalDate.now().plusDays(10)
        createTripResponse.newHotelProductResponse.hotelRoomResponse.freeCancellationWindowDate = checkInDate.toString() + " 23:59"
        sut.freeCancellationText.subscribe(testTextSubscriber)
        createTripResponseObservable.onNext(createTripResponse)

        val formattedCheckInDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(checkInDate)
        assertEquals("Free cancellation before ${formattedCheckInDate}", testTextSubscriber.onNextEvents[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun notPayLaterHoteldueNowIsTotalPrice() {
        givenHappyHotelProductResponse()
        setup()

        createTripResponseObservable.onNext(createTripResponse)

        paymentModel.createTripSubject.onNext(createTripResponse)
        val expectedTotal = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.displayTotalPrice.formattedMoney

        assertEquals(expectedTotal, sut.tripTotalPrice.value)
        assertEquals("$0", sut.dueNowAmount.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun payLaterHotelDueNowIsDepostitAmount() {
        givenPayLaterHotelProductResponse()
        setup()

        createTripResponseObservable.onNext(createTripResponse)

        paymentModel.createTripSubject.onNext(createTripResponse)
        val expectedDueNow = "AUD" + hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.depositAmount

        assertEquals(expectedDueNow, sut.dueNowAmount.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun priceChangeUp() {
        givenPriceChangedUpResponse()
        setup()

        createTripResponseObservable.onNext(createTripResponse)
        paymentModel.createTripSubject.onNext(createTripResponse)
        assertEquals("Price changed from $2,394.88", sut.priceChangeMessage.value)
        assertEquals(R.drawable.warning_triangle_icon, sut.priceChangeIconResourceId.value)
        assertTrue(sut.isPriceChange.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun priceChangeDown() {
        givenPriceChangedDownResponse()
        setup()

        createTripResponseObservable.onNext(createTripResponse)
        paymentModel.createTripSubject.onNext(createTripResponse)
        assertEquals("Price dropped from $2,394.88", sut.priceChangeMessage.value)
        assertEquals(R.drawable.price_change_decrease, sut.priceChangeIconResourceId.value)
        assertTrue(sut.isPriceChange.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBurningRewardPointsForLoggedInUserWithEnoughRedeemablePointsResponse() {
        setup()

        //User is fully paying with points
        givenLoggedInUserWithRedeemablePointsMoreThanTripTotalResponse()

        createTripResponseObservable.onNext(createTripResponse)
        paymentModel.createTripSubject.onNext(createTripResponse)
        assertTrue(sut.isShoppingWithPoints.value)
        assertEquals("$1,000.00", sut.burnAmountShownOnHotelCostBreakdown.value)
        assertEquals("2,500 points", sut.burnPointsShownOnHotelCostBreakdown.value)
        assertEquals("$0.00", sut.tripTotalPrice.value)

        //User changes the payment splits
        val latch1 = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch1.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch1.await(10, TimeUnit.SECONDS)

        assertTrue(sut.isShoppingWithPoints.value)
        assertEquals("$100.00", sut.burnAmountShownOnHotelCostBreakdown.value)
        assertEquals("14,005 points", sut.burnPointsShownOnHotelCostBreakdown.value)
        assertEquals("$3.70", sut.tripTotalPrice.value)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBurningRewardPointsForLoggedInUserWithRedeemablePointsLessThanTripTotalResponse() {
        setup()

        //User is paying with points and card both
        givenLoggedInUserWithRedeemablePointsLessThanTripTotalResponse()
        createTripResponseObservable.onNext(createTripResponse)

        paymentModel.createTripSubject.onNext(createTripResponse)
        assertTrue(sut.isShoppingWithPoints.value)
        assertEquals("$100.00", sut.burnAmountShownOnHotelCostBreakdown.value)
        assertEquals("1,000 points", sut.burnPointsShownOnHotelCostBreakdown.value)
        assertEquals("$35.00", sut.tripTotalPrice.value)
    }

    @Test
    fun testBurningRewardPointsForLoggedInUserWithNonRedeemablePointsResponse() {
        setup()

        //User has less than 3500 points
        givenLoggedInUserWithNonRedeemablePointsResponse()
        createTripResponseObservable.onNext(createTripResponse)

        paymentModel.createTripSubject.onNext(createTripResponse)
        assertFalse(sut.isShoppingWithPoints.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelCheckinCheckoutDateInline() {
        createTripResponse = mockHotelServiceTestRule.getPriceChangeDownCreateTripResponse()
        val params = HotelPresenterTestUtil.getDummyHotelSearchParams(context)
        Db.sharedInstance.abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline.key,
                AbacusUtils.DefaultVariant.BUCKETED.ordinal)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        HotelTracking.trackPageLoadHotelCheckoutInfo(createTripResponse, params, PageUsableData())

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelPayLaterMessagingWithNonPayLaterResponse() {
        setABTestAndFeaturetoggleForPayLaterMessaging(true, true)
        performHotelTrackingWithPayLaterResponse(false)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelPayLaterMessagingWithPayLaterResponse() {
        setABTestAndFeaturetoggleForPayLaterMessaging(true, true)
        performHotelTrackingWithPayLaterResponse(true)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelPayLaterMessagingWithPayLaterResponseAndWithFeatureToggleTurnedOff() {
        setABTestAndFeaturetoggleForPayLaterMessaging(true, false)
        performHotelTrackingWithPayLaterResponse(true)

        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelPayLaterMessagingWithAbacusTestAsControl() {
        setABTestAndFeaturetoggleForPayLaterMessaging(false, true)
        performHotelTrackingWithPayLaterResponse(true)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckOmnitureTrackingForHotelPayLaterMessagingWithAbacusTestAsControlAndFeatureToggleTurnedOff() {
        setABTestAndFeaturetoggleForPayLaterMessaging(false, false)
        performHotelTrackingWithPayLaterResponse(true)

        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging.key), mockAnalyticsProvider)
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(context)
    }

    private fun givenPriceChangedUpResponse() {
        createTripResponse = mockHotelServiceTestRule.getPriceChangeUpCreateTripResponse()
        hotelProductResponse = createTripResponse.originalHotelProductResponse
    }

    private fun givenLoggedInUserWithNonRedeemablePointsResponse() {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemablePointsCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun givenLoggedInUserWithRedeemablePointsMoreThanTripTotalResponse() {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
        createTripResponse.tripId = "happy"
    }

    private fun givenLoggedInUserWithRedeemablePointsLessThanTripTotalResponse() {
        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsLessThanTripTotalCreateTripResponse()
        hotelProductResponse = createTripResponse.originalHotelProductResponse
    }

    private fun givenPriceChangedDownResponse() {
        createTripResponse = mockHotelServiceTestRule.getPriceChangeDownCreateTripResponse()
        hotelProductResponse = createTripResponse.originalHotelProductResponse
    }

    private fun givenPayLaterHotelProductResponse() {
        createTripResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun givenHappyHotelProductResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun givenHappyHotelProductResponseWithPropertyFee() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponseWithPropertyFee()
        hotelProductResponse = createTripResponse.newHotelProductResponse
    }

    private fun setup() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        sut = HotelCheckoutSummaryViewModel(context, paymentModel)
        createTripResponseObservable.subscribe(sut.createTripResponseObservable)
    }

    private fun toggleABTestCheckinCheckoutDatesInline(toggleOn: Boolean) {
        if (toggleOn) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
        } else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
        }
    }

    private fun setABTestAndFeaturetoggleForPayLaterMessaging(payLaterAbTest: Boolean, featureToggle: Boolean) {
        Db.sharedInstance.abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging.key,
                if (payLaterAbTest) AbacusUtils.DefaultVariant.BUCKETED.ordinal else AbacusUtils.DefaultVariant.CONTROL.ordinal)
        Db.sharedInstance.abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline.key,
                AbacusUtils.DefaultVariant.BUCKETED.ordinal)

        SettingUtils.save(context, context.getString(R.string.pay_later_credit_card_messaging), featureToggle)
    }

    private fun performHotelTrackingWithPayLaterResponse(payLaterResponse: Boolean) {
        if (payLaterResponse) {
            createTripResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse()
            createTripResponse.newHotelProductResponse.hotelRoomResponse.depositRequired = false
        } else {
            createTripResponse = mockHotelServiceTestRule.getPriceChangeDownCreateTripResponse()
        }
        val params = HotelPresenterTestUtil.getDummyHotelSearchParams(context)
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        HotelTracking.trackPageLoadHotelCheckoutInfo(createTripResponse, params, PageUsableData())
    }
}
