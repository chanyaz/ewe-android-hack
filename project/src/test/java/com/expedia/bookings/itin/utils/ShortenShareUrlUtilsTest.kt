package com.expedia.bookings.itin.utils

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class ShortenShareUrlUtilsTest {
    lateinit var activity: FlightItinDetailsActivity
    lateinit var itinCardData: ItinCardDataFlight

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("FLIGHT_ITIN_ID", "FLIGHT_ITIN_ID")
        activity = Robolectric.buildActivity(FlightItinDetailsActivity::class.java, intent).create().get()
        itinCardData = ItinCardDataFlightBuilder().build()
    }

    @Test
    fun testShortenSharableUrl() {
        val sut = ShortenShareUrlUtils.getInstance(activity)
        sut.tripShareUrlShortenService = TestShortenUrlService()

        var testSubscriber = TestObserver<String>()
        testSubscriber.assertNoErrors()
        sut.shortenSharableUrl("", testSubscriber)
        testSubscriber.assertValues("")
        testSubscriber.dispose()

        testSubscriber = TestObserver<String>()
        testSubscriber.assertNoErrors()
        sut.shortenSharableUrl("http://expedia.com", testSubscriber)
        testSubscriber.assertValues("m")
        testSubscriber.dispose()
    }

    private class TestShortenUrlService : TripShareUrlShortenServiceInterface {
        override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Disposable =
                Observable.just(TripsShareUrlShortenResponse(url, url.drop(url.length - 1))).subscribeObserver(observer)
    }
}
