package com.expedia.bookings.test

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelCouponViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
public class HotelCouponTest {

    public var service=ServicesRule<HotelServices>(HotelServices::class.java)
        @Rule get

    public var loyaltyServiceRule = ServicesRule<LoyaltyServices>(LoyaltyServices::class.java)
        @Rule get

    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelCouponViewModel(context, service.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
    }

    @Test
    fun couponErrors() {
        val testSubscriber = TestSubscriber<ApiError>(2)
        val expected = arrayListOf<ApiError>()

        vm.errorObservable.take(6).subscribe(testSubscriber)

        var couponParamsBuilder = HotelApplyCouponParams.Builder()
                .tripId("58b6be8a-d533-4eb0-aaa6-0228e000056c")
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(listOf(UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(0, PointsType.BURN, Money()))))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_expired").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Expired"))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_duplicate").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Duplicate"))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_not_active").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "NotActive"))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_not_exists").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "DoesNotExist"))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_not_configured").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "CampaignIsNotConfigured"))

        vm.couponParamsObservable.onNext(couponParamsBuilder.couponCode("hotel_coupon_errors_product_missing").build())
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "PackageProductMissing"))

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertCompleted()
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun applyCouponWithUserPreference() {
        var pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000, PointsType.BURN, Money("100", "USD")))
        var couponParams = HotelApplyCouponParams.Builder()
                .tripId("b33f3017-6f65-4b02-8f73-87c9bf39ea76")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("hotel_coupon_with_user_points_preference")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()

        val testSubscriber = TestSubscriber<HotelCreateTripResponse>()
        vm.couponObservable.subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(couponParams)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        var tripResponse = testSubscriber.onNextEvents[0]
        assertNotNull(tripResponse.coupon)
        assertNotNull(tripResponse.newHotelProductResponse)
        assertNotNull(tripResponse.pointsDetails)
        val userPreferencePoints = tripResponse.userPreferencePoints
        assertNotNull(userPreferencePoints)
        assertNotNull(userPreferencePoints?.amountOnPointsCard)
        assertNotNull(userPreferencePoints?.remainingPayableByCard)
    }

    fun makeErrorInfo(code : ApiError.Code, message : String): ApiError {
        var error = ApiError()
        error.errorCode = code
        error.errorInfo = ApiError.ErrorInfo()
        error.errorInfo.couponErrorType = message
        return error
    }
}
