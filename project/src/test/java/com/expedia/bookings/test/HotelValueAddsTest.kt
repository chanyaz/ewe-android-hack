package com.expedia.bookings.test

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelDetailViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

RunWith(RobolectricRunner::class)
public class HotelValueAddsTest {
    public var server: MockWebServerRule = MockWebServerRule()
        @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelDetailViewModel by Delegates.notNull()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(javaClass<Activity>()).create().get()
        val context = Mockito.mock(javaClass<Context>())
        val resources = activity.getResources()
        Mockito.`when`(context.getResources()).thenReturn(resources)
        val emptyInterceptor = object : RequestInterceptor {
            override fun intercept(request: RequestInterceptor.RequestFacade) {
                // ignore
            }
        }
        service = HotelServices("http://localhost:" + server.getPort(), OkHttpClient(), emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
        vm = HotelDetailViewModel(activity.getApplicationContext(), service)
    }

    private fun setUpTest(): TestSubscriber<HotelOffersResponse> {
        val root = File("../lib/mocked/templates").getCanonicalPath()
        val opener = FileSystemOpener(root)
        server.get().setDispatcher(ExpediaDispatcher(opener))
        val observer = TestSubscriber<HotelOffersResponse>()
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val hotelSearchParams = HotelSearchParams(SuggestionV4(), dtf.parseLocalDate("2015-09-12"), dtf.parseLocalDate("2015-09-16"), 3, emptyList())
        service.details(hotelSearchParams, "happypath", observer)
        return observer
    }

    @Test
    fun verifyCommonRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val offerResponse = observer.getOnNextEvents().get(0)

        val testSubscriber = TestSubscriber<String>()
        val expected = arrayListOf<String>()
        vm.commonAmenityTextObservable.subscribe(testSubscriber)

        vm.hotelOffersSubject.onNext(offerResponse)
        expected.add("All rooms include free airport shuttle")

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }

    @Test
    fun verifyUniqueRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val offerResponse = observer.getOnNextEvents().get(0)
        vm.hotelOffersSubject.onNext(offerResponse)
        var uniqueAmenityForEachRoom = vm.getValueAdd(offerResponse.hotelRoomResponse)
        assertEquals("Includes free parking", uniqueAmenityForEachRoom.get(0))
        assertEquals("Includes continental breakfast", uniqueAmenityForEachRoom.get(1))

    }
}