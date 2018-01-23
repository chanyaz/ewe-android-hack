package com.expedia.vm.hotel

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.urgency.UrgencyResponse
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.squareup.phrase.Phrase
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class UrgencyViewModel(val context: Context, val urgencyService: UrgencyServices) {

    val percentSoldOutTextSubject = PublishSubject.create<String>()
    val urgencyDescriptionSubject = PublishSubject.create<String>()

    private val urgencyResponseObserver = UrgencyObserver()
    private val scoreThreshold: Int = 30
    private val scoreMaximum: Int = 95
    private val invalidRegionId = "0"

    init {
        urgencyResponseObserver.urgencyResponseSubject.subscribe { response ->
            val regionTicker = response.firstRegionTicker
            val rawScore = regionTicker.score

            HotelTracking.trackUrgencyScore(rawScore)
            val displayScore = getDisplayScore(rawScore)

            if (displayScore > scoreThreshold) {
                percentSoldOutTextSubject.onNext(Phrase.from(context, R.string.urgency_percent_booked_TEMPLATE)
                        .put("percentage", displayScore)
                        .format().toString())
                urgencyDescriptionSubject.onNext(Phrase.from(context, R.string.urgency_destination_description_TEMPLATE)
                        .put("destination", regionTicker.displayName)
                        .format().toString())
            }
        }
    }

    fun fetchCompressionScore(regionId: String, checkIn: LocalDate, checkOut: LocalDate) {
        if (isValidRegionId(regionId)) {
            urgencyService.compressionUrgency(regionId, getUrgencyDateFormat(checkIn), getUrgencyDateFormat(checkOut))
                    .subscribe(urgencyResponseObserver)
        }
    }

    @VisibleForTesting
    internal fun getUrgencyDateFormat(date: LocalDate): String {
        return date.toString("MM/d/YYYY")
    }

    private class UrgencyObserver : DisposableObserver<UrgencyResponse>() {
        val urgencyResponseSubject = PublishSubject.create<UrgencyResponse>()

        override fun onNext(response: UrgencyResponse) {
            if (response != null && !response.hasError()) {
                urgencyResponseSubject.onNext(response)
            } else {
                //nothing, swallow errors
            }
        }

        override fun onError(e: Throwable) {
            //nothing, swallow errors
        }

        override fun onComplete() {
        }
    }

    private fun getDisplayScore(score: Int): Int {
        val roundedScore = (5 * (Math.round(score.toDouble() / 5))).toInt()
        if (roundedScore > scoreMaximum) {
            return scoreMaximum
        }
        return roundedScore
    }

    private fun isValidRegionId(id: String): Boolean {
        return !invalidRegionId.equals(id)
    }
}
