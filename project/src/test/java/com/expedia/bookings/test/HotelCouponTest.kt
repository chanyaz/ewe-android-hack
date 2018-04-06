package com.expedia.bookings.test

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelSavedCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.CouponTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CouponWidget
import com.expedia.vm.HotelCouponViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelCouponTest {

    var service = ServicesRule(HotelServices::class.java)
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var vm: HotelCouponViewModel by Delegates.notNull()

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelCouponViewModel(context, service.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
    }

    @Test
    fun couponErrors() {
        val testSubscriber = TestObserver<String>()
        val expected = arrayListOf<String>()

        vm.applyCouponViewModel.errorMessageObservable.subscribe(testSubscriber)

        val couponParamsBuilder = HotelApplyCouponParameters.Builder()
                .tripId("58b6be8a-d533-4eb0-aaa6-0228e000056c")
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(listOf(UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(0f, PointsType.BURN, Money()))))

        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_expired").build(), vm.context.resources.getString(R.string.coupon_error_expired)))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_duplicate").build(), vm.context.resources.getString(R.string.coupon_error_duplicate)))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_active").build(), vm.context.resources.getString(R.string.coupon_error_not_active)))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_exists").build(), vm.context.resources.getString(R.string.coupon_error_unknown)))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_configured").build(), vm.context.resources.getString(R.string.coupon_error_unknown)))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_product_missing").build(), vm.context.resources.getString(R.string.coupon_error_invalid_booking)))

        testSubscriber.assertValueSequence(expected)
    }

    private fun applyCouponWithError(couponParameters: HotelApplyCouponParameters, expectedError: String): String {
        val latch = CountDownLatch(1)
        val subscription = vm.enableSubmitButtonObservable.subscribe { latch.countDown() }
        vm.applyCouponViewModel.applyActionCouponParam.onNext(couponParameters)
        latch.await(10, TimeUnit.SECONDS)
        subscription.dispose()
        return expectedError
    }

    @Test
    fun applyCouponCodeWithUserPreference() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponParameters.Builder()
                .tripId("b33f3017-6f65-4b02-8f73-87c9bf39ea76")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("hotel_coupon_with_user_points_preference")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()

        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.applyCouponViewModel.applyCouponSuccessObservable.subscribe(testSubscriber)

        vm.applyCouponViewModel.applyActionCouponParam.onNext(couponParams)

        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponse = testSubscriber.values()[0]
        assertNotNull(tripResponse.coupon)
        assertNotNull(tripResponse.newHotelProductResponse)
        assertNotNull(tripResponse.pointsDetails)
        val userPreferencePoints = tripResponse.userPreferencePoints
        assertNotNull(userPreferencePoints)
        assertNotNull(userPreferencePoints?.amountOnPointsCard)
        assertNotNull(userPreferencePoints?.remainingPayableByCard)
    }

    @Test
    fun applySavedCouponWithUserPreference() {
        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.storedCouponViewModel.storedCouponSuccessObservable.subscribe(testSubscriber)

        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam())

        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponse = testSubscriber.values()[0]
        assertNotNull(tripResponse.coupon)
        assertEquals("374762687", tripResponse.coupon.instanceId)
    }

    @Test
    fun applySavedCouponAndCouponCodeInParamsWithUserPreference() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelSavedCouponParameters.Builder()
                .tripId("tripId")
                .instanceId("instanceId")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()
        val queryMap = couponParams.toQueryMap()
        assertEquals("instanceId", queryMap["coupon.instanceId"])
        assertNull(queryMap["coupon.code"])
    }

    @Test
    fun removeCouponWorks() {
        val tripId = "happypath_coupon_remove_success"

        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.removeCouponSuccessTrackingInfoObservable.subscribe(testSubscriber)

        vm.couponRemoveObservable.onNext(tripId)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponseWithoutCoupon = testSubscriber.values()[0]
        assertNull(tripResponseWithoutCoupon.coupon)
        assertFalse(vm.removeObservable.value)
        assertFalse(vm.hasDiscountObservable.value)
    }

    @Test
    fun removeCouponFailsWithUnknownError() {
        checkCouponFailure("hotel_coupon_removal_unknown_error", ApiError.Code.UNKNOWN_ERROR)
    }

    @Test
    fun removeCouponFailsWithRemoveError() {
        checkCouponFailure("hotel_coupon_removal_remove_coupon_error", ApiError.Code.REMOVE_COUPON_ERROR)
    }

    @Test
    fun removeCouponFailureWithRetry() {
        val testSubscriberCouponObservable = TestObserver<HotelCreateTripResponse>()
        vm.removeCouponSuccessTrackingInfoObservable.subscribe(testSubscriberCouponObservable)

        val testSubscriberCouponRemoveErrorDialog = TestObserver<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)

        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")
        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)

        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")
        testSubscriberCouponObservable.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponObservable.assertValueCount(1)

        val tripResponseWithoutCoupon = testSubscriberCouponObservable.values()[0]
        assertNull(tripResponseWithoutCoupon.coupon)
        assertFalse(vm.hasDiscountObservable.value)
    }

    @Test
    fun testStoredCouponRemovalErrorTracking() {
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(true))
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove.Error", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(36 to "Removal Error")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "test saved coupon")), mockAnalyticsProvider)
    }

    @Test
    fun testAppliedCouponRemoveErrorTracking() {
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(true))
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove.Error", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(36 to "Removal Error")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "happypath_createtrip_saved_coupons_select")), mockAnalyticsProvider)
    }

    @Test
    fun testSavedCouponRemoveSuccessTracking() {
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(true))
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "test saved coupon")), mockAnalyticsProvider)
    }

    @Test
    fun testApplyCouponRemoveSuccessTracking() {
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(true))
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "happypath_createtrip_saved_coupons_select")), mockAnalyticsProvider)
    }

    @Test
    fun testApplyCouponSuccessObserverDisposedOnceTrackingDone() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(true))
        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove", mockAnalyticsProvider)

        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove", mockAnalyticsProvider)
    }

    @Test
    fun testApplyCouponRemoveObserverWhenRemoveCouponDoneMultipleTimes() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(true))
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove.Error", mockAnalyticsProvider)

        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove.Error", mockAnalyticsProvider)
    }

    @Test
    fun testSavedCouponSuccessTrackingNotCalledMultipleTimes() {
        val testCouponErrorTrackingSubscriber = TestObserver.create<Unit>()
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(true))
        vm.storedCouponViewModel.performCouponSuccessTrackingObservable.subscribe(testCouponErrorTrackingSubscriber)
        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        assertEquals(1, testCouponErrorTrackingSubscriber.valueCount())

        vm.couponRemoveObservable.onNext("happypath_coupon_remove_success")

        assertEquals(2, testCouponErrorTrackingSubscriber.valueCount())
    }

    @Test
    fun testSavedCouponFailureTrackingNotCalledMultipleTimes() {
        val testCouponFailureTrackingSubscriber = TestObserver.create<String>()
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(true))
        vm.storedCouponViewModel.performCouponErrorTrackingObservable.subscribe(testCouponFailureTrackingSubscriber)
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        assertEquals(1, testCouponFailureTrackingSubscriber.valueCount())

        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        assertEquals(2, testCouponFailureTrackingSubscriber.valueCount())
    }

    @Test
    fun testEnteredCouponSuccess() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam())

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Success.Entered", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "happypath_createtrip_saved_coupons_select")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEventsString("event21"), mockAnalyticsProvider)
    }

    @Test
    fun testEnteredCouponFailure() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(false, "hotel_coupon_errors_expired"))

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Fail.Entered", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "hotel_coupon_errors_expired")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEventsString("event22"), mockAnalyticsProvider)
    }

    @Test
    fun testSavedCouponSuccess() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(true, "test saved coupon"))

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Success.Saved", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "test saved coupon")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEventsString("event21"), mockAnalyticsProvider)
    }

    @Test
    fun testSavedCouponFailure() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("hotel_coupon_errors_expired")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(false, "hotel_coupon_errors_expired"))

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Fail.Saved", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "hotel_coupon_errors_expired")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEventsString("event22"), mockAnalyticsProvider)
    }

    @Test
    fun testNetworkErrorIsHandled() {
        val testSubscriber = TestObserver<Unit>()
        vm.applyCouponViewModel.networkErrorAlertDialogObservable.subscribe(testSubscriber)
        vm.applyCouponViewModel.raiseAlertDialog(IOException())

        assertTrue(testSubscriber.valueCount() == 1)
    }

    @Test
    fun testAlertDialogWhenErrorInApplyingCoupon() {
        val couponWidget = setupCouponWidget()
        couponWidget.viewmodel = vm
        vm.applyCouponViewModel.raiseAlertDialog(IOException())
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())

        assertNotNull(alertDialog)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", alertDialog.message)
    }

    @Test
    fun testSessionTimeoutStoredCouponError() {
        val testErrorMessageObservable = TestObserver.create<String>()
        vm.storedCouponViewModel.errorMessageObservable.subscribe(testErrorMessageObservable)
        vm.storedCouponViewModel.storedCouponTrackingObservable.onNext("test saved coupon")
        vm.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(false, "hotel_coupon_session_timeout_error"))

        assertEquals("Sorry, we're having a problem. Please try again.", testErrorMessageObservable.values()[0])
    }

    @Test
    fun testSessionTimeoutApplyCouponError() {
        val testErrorMessageObservable = TestObserver.create<String>()
        vm.applyCouponViewModel.errorMessageObservable.subscribe(testErrorMessageObservable)
        vm.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(false, "hotel_coupon_session_timeout_error"))

        assertEquals("Sorry, we're having a problem. Please try again.", testErrorMessageObservable.values()[0])
    }

    private fun setupCouponWidget(): CouponWidget {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        return LayoutInflater.from(activity).inflate(R.layout.coupon_widget_stub, null) as CouponWidget
    }

    private fun checkCouponFailure(tripId: String, expectedErrorCode: ApiError.Code) {
        val testSubscriberCouponRemoveErrorDialog = TestObserver<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)
        val testSubscriberCouponObservable = TestObserver<HotelCreateTripResponse>()
        vm.removeCouponSuccessTrackingInfoObservable.subscribe(testSubscriberCouponObservable)

        vm.couponRemoveObservable.onNext(tripId)

        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)
        testSubscriberCouponObservable.assertValueCount(0)

        assertEquals(expectedErrorCode, testSubscriberCouponRemoveErrorDialog.values()[0].getErrorCode())
    }
}
