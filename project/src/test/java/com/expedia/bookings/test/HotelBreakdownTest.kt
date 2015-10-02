package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.vm.Breakdown
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue

public class HotelBreakdownTest {
    public var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelBreakDownViewModel by Delegates.notNull()
    private var hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")
        Mockito.`when`(context.getString(Matchers.anyInt())).thenReturn("")
        val emptyInterceptor = object : RequestInterceptor {
            override fun intercept(request: RequestInterceptor.RequestFacade) {
                // ignore
            }
        }
        service = HotelServices("http://localhost:" + server.port, OkHttpClient(), emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
        hotelCheckoutSummaryViewModel = HotelCheckoutSummaryViewModel(context)
        vm = HotelBreakDownViewModel(context, hotelCheckoutSummaryViewModel)
    }

    @Test
    fun verifyBreakdown() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = TestSubscriber<HotelCreateTripResponse>()
        service.createTrip(HotelCreateTripParams("happypath_0", false, 1, emptyList()), observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val createTripResponse = observer.onNextEvents.get(0)

        val latch = CountDownLatch(1)
        vm.addRows.subscribe { latch.countDown() }
        val testSubscriber = TestSubscriber<List<Breakdown>>()
        val expected = arrayListOf<List<Breakdown>>()
        vm.addRows.subscribe(testSubscriber)

        hotelCheckoutSummaryViewModel.newRateObserver.onNext(createTripResponse.newHotelProductResponse)
        expected.add(arrayListOf(Breakdown("", "$99.00", false), Breakdown("3/22/2013", "$99.00", true), Breakdown("", "$20.00", false), Breakdown("", "$16.81", false), Breakdown("", "$135.81", false)))

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        vm.addRows.onCompleted()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }
}
