package com.expedia.vm

import rx.subjects.BehaviorSubject

public class AcceptTermsViewModel() {
    var acceptedTermsObservable = BehaviorSubject.create<Boolean>(false)

    fun resetAcceptedTerms() {
        acceptedTermsObservable.onNext(false)
    }
}
