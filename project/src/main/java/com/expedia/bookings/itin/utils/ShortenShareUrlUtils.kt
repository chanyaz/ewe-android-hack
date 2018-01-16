package com.expedia.bookings.itin.utils

import android.content.Context
import android.util.Log
import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.Subscription

class ShortenShareUrlUtils private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ShortenShareUrlUtils? = null

        fun getInstance(context: Context): ShortenShareUrlUtils =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: createInstance(context).also {
                        INSTANCE = it
                    }
                }

        private fun createInstance(context: Context): ShortenShareUrlUtils = ShortenShareUrlUtils(context)
    }

    var tripShareUrlShortenService: TripShareUrlShortenServiceInterface
    private var tripShareUrlShortenSubscription: Subscription? = null
    private val LOGGING_TAG = "SHORTEN_SHARE_URL"

    init {
        Ui.getApplication(context).defaultTripComponents()
        tripShareUrlShortenService = Ui.getApplication(context).tripComponent().tripShareUrlShortenService()
    }

    //call this method with a url and an observer for the result.
    fun shortenSharableUrl(urlToShorten: String, resultObserver: Observer<String>) {
        if (!urlToShorten.isEmpty()) {
            tripShareUrlShortenSubscription = tripShareUrlShortenService.getShortenedShareUrl(urlToShorten, getShortenUrlObserver(resultObserver))
        } else {
            resultObserver.onNext("")
        }
    }

    private fun getShortenUrlObserver(resultObserver: Observer<String>): Observer<TripsShareUrlShortenResponse> {
        return object : Observer<TripsShareUrlShortenResponse> {
            override fun onCompleted() {
                tripShareUrlShortenSubscription?.unsubscribe()
            }

            override fun onError(e: Throwable?) {
                Log.d(LOGGING_TAG, "Error: " + e?.printStackTrace().toString())
                resultObserver.onError(e)
            }

            override fun onNext(t: TripsShareUrlShortenResponse?) {
                val shortUrl = t?.short_url
                Log.d(LOGGING_TAG, "Success: " + shortUrl)
                resultObserver.onNext(shortUrl)
            }
        }
    }
}