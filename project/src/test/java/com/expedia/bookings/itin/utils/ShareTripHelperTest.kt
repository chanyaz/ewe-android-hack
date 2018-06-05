package com.expedia.bookings.itin.utils

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ShareTripHelperTest {

//    @Test
//    fun testShortenUrlAndShowDialog() {
//        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
//        val shareTripHelper = ShareTripHelper(activity)
//        shareTripHelper.shortenShareUtil.tripShareUrlShortenService = TestShortenUrlService()
//        shareTripHelper.shortenUrl("https://expedia.com", "I'm going to LA", "trip")
//
//        val shadowActivity = Shadows.shadowOf(activity)
//        val intent = shadowActivity.peekNextStartedActivityForResult().intent
//
//        assertEquals("I'm going to LA", intent.getStringExtra(Intent.EXTRA_TEXT))
//    }
//
//    private class TestShortenUrlService : TripShareUrlShortenServiceInterface {
//        override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Disposable =
//                Observable.just(TripsShareUrlShortenResponse(url, "https://e.xp.co")).subscribeObserver(observer)
//    }
}
