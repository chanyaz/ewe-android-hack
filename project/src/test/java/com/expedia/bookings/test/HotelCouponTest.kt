package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.vm.HotelCouponViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import rx.observers.TestSubscriber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue

public class HotelCouponTest {

    public var service: HotelServicesRule = HotelServicesRule()
    @Rule get

    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(javaClass<Context>())
        val resources = Mockito.mock(javaClass<Resources>())
        Mockito.`when`(context.getResources()).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")

        vm = HotelCouponViewModel(context, service.hotelServices())
    }

    @Test
    fun couponErrors() {
        val testSubscriber = TestSubscriber<ApiError>(2)
        val expected = arrayListOf<ApiError>()

        vm.errorObservable.take(2).subscribe(testSubscriber)

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_expired"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Expired"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_duplicate"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Duplicate"))

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
