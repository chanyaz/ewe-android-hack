package com.expedia.bookings.test

import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
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

@RunWith(RobolectricRunner::class)
public class HotelCouponTest {

    public var service: HotelServicesRule = HotelServicesRule()
    @Rule get

    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelCouponViewModel(context, service.hotelServices())
    }

    @Test
    fun couponErrors() {
        val testSubscriber = TestSubscriber<ApiError>(2)
        val expected = arrayListOf<ApiError>()

        vm.errorObservable.take(6).subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_expired"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Expired"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_duplicate"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Duplicate"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_not_active"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "NotActive"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_not_exists"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "DoesNotExist"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_not_configured"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "CampaignIsNotConfigured"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_product_missing"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "PackageProductMissing"))

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertCompleted()
        testSubscriber.assertReceivedOnNext(expected)
    }

    fun makeErrorInfo(code : ApiError.Code, message : String): ApiError {
        var error = ApiError()
        error.errorCode = code
        error.errorInfo = ApiError.ErrorInfo()
        error.errorInfo.couponErrorType = message
        return error
    }
}
