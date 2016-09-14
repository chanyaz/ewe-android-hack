package com.expedia.vm

import com.expedia.vm.rail.RailCreditCardFeesViewModel
import rx.subjects.BehaviorSubject

open abstract class BaseCreditCardFeesViewModel {
    // output
    val cardFeesObservable = BehaviorSubject.create<List<RailCreditCardFeesViewModel.CardFeesRow>>()
}