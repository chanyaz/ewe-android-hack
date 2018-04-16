package com.expedia.vm.flights

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightCheckoutSummaryViewModel(val context: Context) {
    val showFreeCancellationObservable = BehaviorSubject.createDefault(false)
    val showSplitTicketMessagingObservable = BehaviorSubject.createDefault(false)
    val splitTicketBaggageFeesLinksObservable = BehaviorSubject.create<SpannableStringBuilder>()
    val showAirlineFeeWarningObservable = BehaviorSubject.createDefault<Boolean>(false)
    val airlineFeeWarningTextObservable = BehaviorSubject.create<String>()
    val showBasicEconomyMessageObservable = BehaviorSubject.createDefault(false)
    var outboundSelectedAndTotalLegRank: Pair<Int, Int>? = null
    var inboundSelectedAndTotalLegRank: Pair<Int, Int>? = null
    val evolableTermsConditionTextObservable = PublishSubject.create<SpannableStringBuilder>()
    val evolableTermsConditionSubject = PublishSubject.create<FlightTripDetails.FlightOffer>()
    val obFeeDetailsUrlObservable = PublishSubject.create<String>()
    val showWebviewCheckoutObservable = PublishSubject.create<Unit>()

    init {
        evolableTermsConditionSubject.filter { flightOffer ->
            flightOffer.isEvolable && flightOffer.evolableUrls != null && isEvolableEnabled()
        }.map { it.evolableUrls }.subscribe { flightEvolableUrl ->
            val baggageFeesTextFormatted = Phrase.from(context, R.string.evolable_terms_condition_TEMPLATE)
                    .put("evolable_asia_link", flightEvolableUrl.evolableAsiaUrl)
                    .put("evolable_terms_condition_link", flightEvolableUrl.evolableTermsAndConditionsUrl)
                    .format().toString()

            val baggageFeesTextWithColoredClickableLinks = StrUtils.getSpannableTextByColor(baggageFeesTextFormatted,
                    ContextCompat.getColor(context, R.color.flight_primary_color), true)

            evolableTermsConditionTextObservable.onNext(baggageFeesTextWithColoredClickableLinks)
        }
    }

    fun isEvolableEnabled(): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    }
}
