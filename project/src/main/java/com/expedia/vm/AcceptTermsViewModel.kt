package com.expedia.vm

import rx.subjects.PublishSubject

public class AcceptTermsViewModel() {
    var acceptedTermsObservable = PublishSubject.create<Unit>()
}
