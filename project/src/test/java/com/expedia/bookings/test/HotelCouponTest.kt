package com.expedia.bookings.test

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelApplyCouponCodeParameters
import com.expedia.bookings.data.hotels.HotelApplySavedCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CouponWidget
import com.expedia.vm.HotelCouponViewModel
import io.reactivex.Observable
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
        val testSubscriber = TestObserver<ApiError>()
        val s = TestObserver<Observable<HotelCreateTripResponse>>()
        val expected = arrayListOf<ApiError>()

        vm.errorObservable.subscribe(testSubscriber)
        vm.createTripDownloadsObservable.subscribe(s)

        val couponParamsBuilder = HotelApplyCouponCodeParameters.Builder()
                .tripId("58b6be8a-d533-4eb0-aaa6-0228e000056c")
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(listOf(UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(0f, PointsType.BURN, Money()))))

        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_expired").build(), "Expired"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_duplicate").build(), "Duplicate"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_active").build(), "NotActive"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_exists").build(), "DoesNotExist"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_configured").build(), "CampaignIsNotConfigured"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_product_missing").build(), "PackageProductMissing"))

        testSubscriber.assertValueSequence(expected)
    }

    private fun applyCouponWithError(couponCodeParameters: HotelApplyCouponCodeParameters, expectedError: String): ApiError {
        val latch = CountDownLatch(1)
        val subscription = vm.enableSubmitButtonObservable.subscribe { latch.countDown() }
        vm.couponParamsObservable.onNext(couponCodeParameters)
        latch.await(10, TimeUnit.SECONDS)
        subscription.dispose()
        return makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, expectedError)
    }

    @Test
    fun applyCouponCodeWithUserPreference() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponCodeParameters.Builder()
                .tripId("b33f3017-6f65-4b02-8f73-87c9bf39ea76")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("hotel_coupon_with_user_points_preference")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()

        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(couponParams)

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
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplySavedCodeParameters.Builder()
                .tripId("tripId")
                .instanceId("happypath_createtrip_saved_coupons_select")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()

        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.storedCouponSuccessObservable.subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(couponParams)

        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponse = testSubscriber.values()[0]
        assertNotNull(tripResponse.coupon)
        assertEquals("374762687", tripResponse.coupon.instanceId)
    }

    @Test
    fun applySavedCouponAndCouponCodeInParamsWithUserPreference() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplySavedCodeParameters.Builder()
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
        val tripId = "hotel_coupon_remove_success"

        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriber)

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
        vm.couponObservable.subscribe(testSubscriberCouponObservable)

        val testSubscriberCouponRemoveErrorDialog = TestObserver<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)

        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")
        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)

        vm.couponRemoveObservable.onNext("hotel_coupon_remove_success")
        testSubscriberCouponObservable.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponObservable.assertValueCount(1)

        val tripResponseWithoutCoupon = testSubscriberCouponObservable.values()[0]
        assertNull(tripResponseWithoutCoupon.coupon)
        assertFalse(vm.hasDiscountObservable.value)
    }

    @Test
    fun testCouponRemovalErrorTracking() {
        val savedCoupon = HotelCreateTripResponse.SavedCoupon()
        savedCoupon.name = "test saved coupon"
        savedCoupon.instanceId = "12345"

        vm.storedCouponApplyObservable.onNext(savedCoupon)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Remove.Error", mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(36 to "Removal Error")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(24 to "test saved coupon")), mockAnalyticsProvider)
    }

    @Test
    fun testNetworkErrorIsHandled() {
        val testSubscriber = TestObserver<Unit>()
        vm.networkErrorAlertDialogObservable.subscribe(testSubscriber)
        vm.raiseAlertDialog(IOException())

        assertTrue(testSubscriber.valueCount() == 1)
    }

    @Test
    fun testAlertDialogWhenErrorInApplyingCoupon() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        val couponWidget = LayoutInflater.from(activity).inflate(R.layout.coupon_widget_stub, null) as CouponWidget
        couponWidget.viewmodel = vm
        vm.raiseAlertDialog(IOException())
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())

        assertNotNull(alertDialog)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", alertDialog.message)
    }

    private fun checkCouponFailure(tripId: String, expectedErrorCode: ApiError.Code) {
        val testSubscriberCouponRemoveErrorDialog = TestObserver<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)
        val testSubscriberCouponObservable = TestObserver<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriberCouponObservable)

        vm.couponRemoveObservable.onNext(tripId)

        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)
        testSubscriberCouponObservable.assertValueCount(0)

        assertEquals(expectedErrorCode, testSubscriberCouponRemoveErrorDialog.values()[0].errorCode)
    }

    fun makeErrorInfo(code: ApiError.Code, message: String): ApiError {
        val error = ApiError()
        error.errorCode = code
        error.errorInfo = ApiError.ErrorInfo()
        error.errorInfo.couponErrorType = message
        return error
    }
}
