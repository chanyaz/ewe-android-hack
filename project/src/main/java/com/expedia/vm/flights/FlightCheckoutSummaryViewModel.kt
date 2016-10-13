package com.expedia.vm.flights

import android.text.SpannableStringBuilder
import rx.subjects.BehaviorSubject


class FlightCheckoutSummaryViewModel {
    val showFreeCancellationObservable = BehaviorSubject.create(false)
    val showSplitTicketMessagingObservable = BehaviorSubject.create(false)
    val splitTicketBaggageFeesLinksObservable = BehaviorSubject.create<SpannableStringBuilder>()
    val showAirlineFeeWarningObservable = BehaviorSubject.create(false)
    val airlineFeeWarningTextObservable = BehaviorSubject.create<String>()
}
