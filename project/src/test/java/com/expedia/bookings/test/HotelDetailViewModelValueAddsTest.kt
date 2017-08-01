package com.expedia.bookings.test

import android.app.Activity
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.hotel.HotelDetailViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import io.reactivex.schedulers.Schedulers
import java.io.File
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelDetailViewModelValueAddsTest {
    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: HotelServices by Delegates.notNull()
    private var vm: HotelDetailViewModel by Delegates.notNull()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = HotelServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
<<<<<<< HEAD
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        vm = HotelDetailViewModel(activity.applicationContext,
                HotelInfoManager(Mockito.mock(HotelServices::class.java)))
=======
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
        vm = HotelDetailViewModel(activity.applicationContext)
>>>>>>> 0e8f08fc73... WIP
    }

    private fun setUpTest(): TestObserver<HotelOffersResponse> {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = TestObserver<HotelOffersResponse>()
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val suggestion = SuggestionV4()
        suggestion.coordinates = SuggestionV4.LatLng()
        val hotelSearchParams = HotelSearchParams.Builder(0, 0)
                .destination(suggestion).startDate(dtf.parseLocalDate("2015-09-12"))
                .endDate(dtf.parseLocalDate("2015-09-16"))
                .adults(3)
                .children(emptyList()).build() as HotelSearchParams
        vm.paramsSubject.onNext(hotelSearchParams)
        service.offers(hotelSearchParams, "happypath", observer)
        return observer
    }

    @Test
    fun verifyCommonRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertComplete()

        val offerResponse = observer.onNextEvents[0]

        val testSubscriber = TestObserver<String>()
        val expected = arrayListOf<String>()
        vm.commonAmenityTextObservable.subscribe(testSubscriber)

        vm.hotelOffersSubject.onNext(offerResponse)
        vm.addViewsAfterTransition()

        expected.add("All rooms include free airport shuttle")

        testSubscriber.assertReceivedOnNext(expected)
        testSubscriber.dispose()
    }

    @Test
    fun verifyUniqueRoomAmenities() {
        val observer = setUpTest()
        observer.awaitTerminalEvent()
        observer.assertComplete()

        val offerResponse = observer.onNextEvents[0]
        vm.hotelOffersSubject.onNext(offerResponse)
        val uniqueAmenityForEachRoom = vm.getValueAdd(offerResponse.hotelRoomResponse)
        assertEquals("Includes free parking", uniqueAmenityForEachRoom[0])
        assertEquals("Includes continental breakfast", uniqueAmenityForEachRoom[1])

    }
}
