package com.expedia.vm.flights

import android.text.SpannableStringBuilder
import io.reactivex.subjects.BehaviorSubject


class FlightCheckoutSummaryViewModel {
    val showFreeCancellationObservable = BehaviorSubject.createDefault<Boolean>(false)
    val showSplitTicketMessagingObservable = BehaviorSubject.createDefault<Boolean>(false)
    val splitTicketBaggageFeesLinksObservable = BehaviorSubject.create<SpannableStringBuilder>()
    val showAirlineFeeWarningObservable = BehaviorSubject.createDefault<Boolean>(false)
    val airlineFeeWarningTextObservable = BehaviorSubject.create<String>()
    val showBasicEconomyMessageObservable = BehaviorSubject.createDefault<Boolean>(false)
    var outboundSelectedAndTotalLegRank: Pair<Int, Int>? =  null
    var inboundSelectedAndTotalLegRank: Pair<Int, Int>? =  null
}
