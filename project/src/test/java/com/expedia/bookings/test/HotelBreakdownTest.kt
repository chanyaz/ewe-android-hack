package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.vm.Breakdown
import com.expedia.vm.HotelBreakDownViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
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

public class HotelBreakdownTest {
    public var server: MockWebServerRule = MockWebServerRule()
        @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelBreakDownViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        Mockito.`when`(context.getResources()).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")
        Mockito.`when`(context.getString(Matchers.anyInt())).thenReturn("")
        val emptyInterceptor = object : RequestInterceptor {
            override fun intercept(request: RequestInterceptor.RequestFacade) {
                // ignore
            }
        }
        service = HotelServices("http://localhost:" + server.port, OkHttpClient(), emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
        vm = HotelBreakDownViewModel(context)
    }

    @Test
    fun verifyBreakdown() {
        val root = File("../lib/mocked/templates").getCanonicalPath()
        val opener = FileSystemOpener(root)
        server.get().setDispatcher(ExpediaDispatcher(opener))
        val observer = TestSubscriber<HotelCreateTripResponse>()
        service.createTrip(HotelCreateTripParams("happypath_0", false, 1, emptyList()), observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val createTripResponse = observer.getOnNextEvents().get(0)

        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }
        val testSubscriber = TestSubscriber<List<Breakdown>>()
        val expected = arrayListOf<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        vm.tripObserver.onNext(createTripResponse)
        expected.add(arrayListOf(Breakdown("", "$119.00", false), Breakdown("3/22/2013", "$119.00", true), Breakdown("", "$20.00", false), Breakdown("", "$16.81", false), Breakdown("", "$135.81", false)))

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onCompleted()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }
}
