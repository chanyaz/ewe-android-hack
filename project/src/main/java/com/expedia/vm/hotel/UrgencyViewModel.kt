package com.expedia.vm.hotel

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.urgency.UrgencyResponse
import com.expedia.bookings.services.urgency.UrgencyServices
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject

class UrgencyViewModel(val context: Context, val urgencyService: UrgencyServices) {

    val rawSoldOutScoreSubject = PublishSubject.create<Int>()
    val percentSoldOutTextSubject = PublishSubject.create<String>()
    val urgencyDescriptionSubject = PublishSubject.create<String>()

    private val urgencyResponseObserver = UrgencyObserver()
    private val scoreThreshold: Int = 30
    private val invalidRegionId = "0";

    init {
        urgencyResponseObserver.scoreSubject.subscribe(rawSoldOutScoreSubject)
        urgencyResponseObserver.scoreSubject.subscribe { score ->
            val roundedScore = getRoundedScore(score)
            if (roundedScore > scoreThreshold) {
                percentSoldOutTextSubject.onNext(Phrase.from(context, R.string.urgency_percent_booked_TEMPLATE)
                        .put("percentage", roundedScore)
                        .format().toString())
            }
        }
        urgencyResponseObserver.displayNameSubject.subscribe { displayName ->
            urgencyDescriptionSubject.onNext(Phrase.from(context, R.string.urgency_destination_description_TEMPLATE)
                    .put("destination", displayName)
                    .format().toString())
        }
    }

    fun fetchCompressionScore(regionId: String, checkIn: LocalDate, checkOut: LocalDate) {
        if (isValidRegionId(regionId)) {
            urgencyService.compressionUrgency(regionId, getUrgencyDateFormat(checkIn), getUrgencyDateFormat(checkOut))
                    .subscribe(urgencyResponseObserver)
        }
    }

    @VisibleForTesting
    internal fun getUrgencyDateFormat(date: LocalDate) : String {
        return date.toString("MM/d/YYYY")
    }

    private class UrgencyObserver : Observer<UrgencyResponse> {
        val scoreSubject = PublishSubject.create<Int>()
        val displayNameSubject = PublishSubject.create<String>()

        override fun onNext(response: UrgencyResponse?) {
            if (response != null && !response.hasError()) {
                val regionTicker = response.firstRegionTicker
                scoreSubject.onNext(regionTicker.score)
                displayNameSubject.onNext((regionTicker.displayName))
            } else {
                //nothing, swallow errors
            }
        }

        override fun onError(e: Throwable?) {
            //nothing, swallow errors
        }

        override fun onCompleted() {
        }
    }

    private fun getRoundedScore(score: Int) : Int {
        return (5 * (Math.round(score.toDouble() / 5))).toInt()
    }

    private fun isValidRegionId(id: String) : Boolean {
        return !invalidRegionId.equals(id)
    }
}
