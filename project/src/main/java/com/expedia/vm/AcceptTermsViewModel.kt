package com.expedia.vm

import io.reactivex.subjects.BehaviorSubject

class AcceptTermsViewModel {
    var acceptedTermsObservable = BehaviorSubject.createDefault<Boolean>(false)

    fun resetAcceptedTerms() {
        acceptedTermsObservable.onNext(false)
    }
}
