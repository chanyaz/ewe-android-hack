package com.expedia.bookings.itin.utils

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.observers.TestSubscriber

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

        var testSubscriber = TestSubscriber<String>()
        testSubscriber.assertNoErrors()
        sut.shortenSharableUrl("", testSubscriber)
        testSubscriber.assertReceivedOnNext(mutableListOf(""))
        testSubscriber.unsubscribe()

        testSubscriber = TestSubscriber<String>()
        testSubscriber.assertNoErrors()
        sut.shortenSharableUrl("http://expedia.com", testSubscriber)
        testSubscriber.assertReceivedOnNext(mutableListOf("m"))
        testSubscriber.unsubscribe()
    }

    private class TestShortenUrlService : TripShareUrlShortenServiceInterface {
        override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Subscription =
                Observable.just(TripsShareUrlShortenResponse(url, url.drop(url.length - 1))).subscribe(observer)
    }
}
