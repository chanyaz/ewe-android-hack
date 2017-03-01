package com.expedia.vm.hotel

import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.urgency.UrgencyResponse
import com.expedia.bookings.services.urgency.UrgencyServices
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject

class UrgencyViewModel(val urgencyService: UrgencyServices) {
    val percentSoldOutScoreSubject = PublishSubject.create<Int>()

    private val urgencyResponseObserver: UrgencyObserver

    init {
        urgencyResponseObserver = UrgencyObserver()
        urgencyResponseObserver.scoreSubject.subscribe(percentSoldOutScoreSubject)
    }

    fun fetchCompressionScore(regionId: String, checkIn: LocalDate, checkOut: LocalDate) {
        urgencyService.compressionUrgency(regionId, getUrgencyDateFormat(checkIn), getUrgencyDateFormat(checkOut))
                .subscribe(urgencyResponseObserver)
    }

    @VisibleForTesting
    internal fun getUrgencyDateFormat(date: LocalDate) : String {
        return date.toString("MM/d/YYYY")
    }

    private class UrgencyObserver : Observer<UrgencyResponse> {
        val scoreSubject = PublishSubject.create<Int>()

        override fun onNext(response: UrgencyResponse?) {
            if (response != null && !response.hasError()) {
                val regionTicker = response.firstRegionTicker
                scoreSubject.onNext(regionTicker.score)
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
}
