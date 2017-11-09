package com.expedia.vm.flights

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCheckoutSummaryViewModel(val context: Context) {
    val showFreeCancellationObservable = BehaviorSubject.create(false)
    val showSplitTicketMessagingObservable = BehaviorSubject.create(false)
    val splitTicketBaggageFeesLinksObservable = BehaviorSubject.create<SpannableStringBuilder>()
    val showAirlineFeeWarningObservable = BehaviorSubject.create(false)
    val airlineFeeWarningTextObservable = BehaviorSubject.create<String>()
    val showBasicEconomyMessageObservable = BehaviorSubject.create(false)
    var outboundSelectedAndTotalLegRank: Pair<Int, Int>? = null
    var inboundSelectedAndTotalLegRank: Pair<Int, Int>? = null
    val evolableTermsConditionTextObservable = PublishSubject.create<SpannableStringBuilder>()
    val evolableTermsConditionSubject = PublishSubject.create<List<FlightLeg>>()

    init {
        evolableTermsConditionSubject.filter { flightList ->
            val flightLeg = flightList?.firstOrNull()
            flightLeg != null && flightLeg.isEvolable && isEvolableEnabled()
        }.map { flightList ->
            flightList[0]
        }.subscribe { flightLeg ->
            val baggageFeesTextFormatted = Phrase.from(context, R.string.evolable_terms_condition_TEMPLATE)
                    .put("evolable_asia_link", flightLeg.evolableAsiaUrl)
                    .put("evolable_terms_condition_link", flightLeg.evolableTermsAndConditionsUrl)
                    .format().toString()

            val baggageFeesTextWithColoredClickableLinks = StrUtils.getSpannableTextByColor(baggageFeesTextFormatted,
                    ContextCompat.getColor(context, R.color.flight_primary_color), true)

            evolableTermsConditionTextObservable.onNext(baggageFeesTextWithColoredClickableLinks)
        }
    }

    fun isEvolableEnabled(): Boolean {
        return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    }
}
