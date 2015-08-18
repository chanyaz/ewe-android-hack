package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.services.HotelServices
import com.expedia.vm.HotelCouponViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue

public class HotelCouponTest {
    private var LOTS_MORE: Long = 100

    public var server: MockWebServerRule = MockWebServerRule()
    @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(javaClass<Context>())
        val emptyInterceptor = object : RequestInterceptor {
            override fun intercept(request: RequestInterceptor.RequestFacade) {
                // ignore
            }
        }
        service = HotelServices("http://localhost:" + server.getPort(), OkHttpClient(), emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
        vm = HotelCouponViewModel(context, service)
    }

    @Test
    fun couponErrors() {
        val root = File("../lib/mocked/templates").getCanonicalPath()
        val opener = FileSystemOpener(root)
        server.get().setDispatcher(ExpediaDispatcher(opener))

        val testSubscriber = TestSubscriber<ApiError>()
        val expected = arrayListOf<ApiError>()
        val latch = CountDownLatch(2)

        vm.errorObservable.subscribe(testSubscriber)
        vm.errorObservable.subscribe { latch.countDown() }

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_expired"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Expired"))

        vm.couponParamsObservable.onNext(HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", "hotel_coupon_errors_duplicate"))
        expected.add(makeErrorInfo(ApiError.Code.APPLY_COUPON_ERROR, "Duplicate"))

        testSubscriber.requestMore(LOTS_MORE)
        assertTrue(latch.await(10, TimeUnit.SECONDS))
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
