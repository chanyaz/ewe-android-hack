package com.expedia.bookings.test

import android.app.Activity
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelValueAddsTest {
    public var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelDetailViewModel by Delegates.notNull()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        service = HotelServices("http://localhost:" + server.port, OkHttpClient(), MockInterceptor(), Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
        vm = HotelDetailViewModel(activity.applicationContext, service, endlessObserver { /*ignore*/ })
    }

    private fun setUpTest(): TestSubscriber<HotelOffersResponse> {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = TestSubscriber<HotelOffersResponse>()
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val hotelSearchParams = HotelSearchParams(SuggestionV4(), dtf.parseLocalDate("2015-09-12"), dtf.parseLocalDate("2015-09-16"), 3, emptyList())
        vm.paramsSubject.onNext(hotelSearchParams)
        service.offers(hotelSearchParams, "happypath", observer)
        return observer
    }

    @Test
    fun verifyCommonRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val offerResponse = observer.onNextEvents.get(0)

        val testSubscriber = TestSubscriber<String>()
        val expected = arrayListOf<String>()
        vm.commonAmenityTextObservable.subscribe(testSubscriber)

        vm.hotelOffersSubject.onNext(offerResponse)
        vm.addViewsAfterTransition()

        expected.add("All rooms include free airport shuttle")

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.unsubscribe()
    }

    @Test
    fun verifyUniqueRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertCompleted()

        val offerResponse = observer.onNextEvents.get(0)
        vm.hotelOffersSubject.onNext(offerResponse)
        var uniqueAmenityForEachRoom = vm.getValueAdd(offerResponse.hotelRoomResponse)
        assertEquals("Includes free parking", uniqueAmenityForEachRoom.get(0))
        assertEquals("Includes continental breakfast", uniqueAmenityForEachRoom.get(1))

    }
}
