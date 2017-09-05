package com.expedia.bookings.test

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCouponViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelCouponTest {

    var service= ServicesRule(HotelServices::class.java)
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelCouponViewModel(context, service.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
    }

    @Test
    fun couponErrors() {
        val testSubscriber = TestSubscriber<ApiError>()
        val s = TestSubscriber<Observable<HotelCreateTripResponse>>()
        val expected = arrayListOf<ApiError>()

        vm.errorObservable.subscribe(testSubscriber)
        vm.createTripDownloadsObservable.subscribe(s)

        val couponParamsBuilder = HotelApplyCouponParameters.Builder()
                .tripId("58b6be8a-d533-4eb0-aaa6-0228e000056c")
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(listOf(UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(0f, PointsType.BURN, Money()))))

        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_expired").build(), "Expired"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_duplicate").build(), "Duplicate"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_active").build(), "NotActive"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_exists").build(), "DoesNotExist"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_not_configured").build(), "CampaignIsNotConfigured"))
        expected.add(applyCouponWithError(couponParamsBuilder.couponCode("hotel_coupon_errors_product_missing").build(), "PackageProductMissing"))

        testSubscriber.assertReceivedOnNext(expected)
    }

    private fun applyCouponWithError(couponParameters: HotelApplyCouponParameters, expectedError: String): ApiError {
        val latch = CountDownLatch(1)
        val subscription = vm.enableSubmitButtonObservable.subscribe { latch.countDown() }
        vm.couponParamsObservable.onNext(couponParameters)
        latch.await(10, TimeUnit.SECONDS)
        subscription.unsubscribe()
        return makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, expectedError)
    }

    @Test
    fun applyCouponWithUserPreference() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponParameters.Builder()
                .tripId("b33f3017-6f65-4b02-8f73-87c9bf39ea76")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("hotel_coupon_with_user_points_preference")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()

        val testSubscriber = TestSubscriber<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(couponParams)

        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponse = testSubscriber.onNextEvents[0]
        assertNotNull(tripResponse.coupon)
        assertNotNull(tripResponse.newHotelProductResponse)
        assertNotNull(tripResponse.pointsDetails)
        val userPreferencePoints = tripResponse.userPreferencePoints
        assertNotNull(userPreferencePoints)
        assertNotNull(userPreferencePoints?.amountOnPointsCard)
        assertNotNull(userPreferencePoints?.remainingPayableByCard)
    }

    @Test
    fun removeCouponWorks() {
        val tripId = "hotel_coupon_remove_success"

        val testSubscriber = TestSubscriber<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriber)

        vm.couponRemoveObservable.onNext(tripId)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        val tripResponseWithoutCoupon = testSubscriber.onNextEvents[0]
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
        val testSubscriberCouponObservable = TestSubscriber<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriberCouponObservable)

        val testSubscriberCouponRemoveErrorDialog = TestSubscriber<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)

        vm.couponRemoveObservable.onNext("hotel_coupon_removal_remove_coupon_error")
        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)

        vm.couponRemoveObservable.onNext("hotel_coupon_remove_success")
        testSubscriberCouponObservable.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponObservable.assertValueCount(1)

        val tripResponseWithoutCoupon = testSubscriberCouponObservable.onNextEvents[0]
        assertNull(tripResponseWithoutCoupon.coupon)
        assertFalse(vm.hasDiscountObservable.value)
    }

    private fun checkCouponFailure(tripId: String, expectedErrorCode: ApiError.Code) {
        val testSubscriberCouponRemoveErrorDialog = TestSubscriber<ApiError>()
        vm.errorRemoveCouponShowDialogObservable.subscribe(testSubscriberCouponRemoveErrorDialog)
        val testSubscriberCouponObservable = TestSubscriber<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriberCouponObservable)

        vm.couponRemoveObservable.onNext(tripId)

        testSubscriberCouponRemoveErrorDialog.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriberCouponRemoveErrorDialog.assertValueCount(1)
        testSubscriberCouponObservable.assertValueCount(0)

        assertEquals(expectedErrorCode, testSubscriberCouponRemoveErrorDialog.onNextEvents[0].errorCode)
    }

    fun makeErrorInfo(code : ApiError.Code, message : String): ApiError {
        val error = ApiError()
        error.errorCode = code
        error.errorInfo = ApiError.ErrorInfo()
        error.errorInfo.couponErrorType = message
        return error
    }
}
