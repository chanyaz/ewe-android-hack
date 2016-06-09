package com.expedia.vm.flights

import android.text.SpannableStringBuilder
import rx.subjects.BehaviorSubject


class FlightOverviewViewModel {
    val showFreeCancellationObservable = BehaviorSubject.create(false)
    val showSplitTicketMessagingObservable = BehaviorSubject.create(false)
    val splitTicketBaggageFeesLinksObservable = BehaviorSubject.create<SpannableStringBuilder>()
}
