package com.expedia.bookings.hotel.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.urgency.UrgencyResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.squareup.phrase.Phrase
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class UrgencyViewModel(val context: Context, val urgencyService: UrgencyServices) {

    val urgencyTextSubject = PublishSubject.create<String>()

    private val urgencyResponseSubject = PublishSubject.create<UrgencyResponse>()
    private val scoreMaximum: Int = 95
    private val invalidRegionId = "0"

    private val variantOneThreshold = 50
    private val variantTwoThreshold = 30

    init {
        urgencyResponseSubject.subscribe { response ->
            val regionTicker = response.firstRegionTicker
            val rawScore = regionTicker.score

            HotelTracking.trackUrgencyScore(rawScore)
            val displayScore = getDisplayScore(rawScore)

            if (isScoreUnderThreshold(displayScore)) {
                urgencyTextSubject.onNext(Phrase.from(context, R.string.urgency_only_x_hotels_left_TEMPLATE)
                        .put("percent", displayScore)
                        .format().toString())
            }
        }
    }

    fun fetchCompressionScore(regionId: String, checkIn: LocalDate, checkOut: LocalDate) {
        if (isValidRegionId(regionId)) {
            urgencyService.compressionUrgency(regionId, getUrgencyDateFormat(checkIn), getUrgencyDateFormat(checkOut))
                    .subscribe(UrgencyObserver(urgencyResponseSubject))
        }
    }

    @VisibleForTesting
    internal fun getUrgencyDateFormat(date: LocalDate): String {
        return date.toString("MM/d/YYYY")
    }

    private class UrgencyObserver(val urgencyResponseSubject: PublishSubject<UrgencyResponse>)
        : DisposableObserver<UrgencyResponse>() {

        override fun onNext(response: UrgencyResponse) {
            if (!response.hasError()) {
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

    private fun isScoreUnderThreshold(score: Int): Boolean {
        if (isInVariant(AbacusVariant.ONE) && score <= variantOneThreshold) {
            return true
        } else if (isInVariant(AbacusVariant.TWO) && score <= variantTwoThreshold) {
            return true
        }
        return false
    }

    private fun getDisplayScore(score: Int): Int {
        val roundedScore = (5 * (Math.round(score.toDouble() / 5))).toInt()
        if (roundedScore > scoreMaximum) {
            return 100 - scoreMaximum
        }
        return 100 - roundedScore
    }

    private fun isValidRegionId(id: String): Boolean {
        return !invalidRegionId.equals(id)
    }

    private fun isInVariant(variant: AbacusVariant): Boolean {
        return AbacusFeatureConfigManager.isBucketedForVariant(context, AbacusUtils.HotelUrgencyV2,
                variant)
    }
}
