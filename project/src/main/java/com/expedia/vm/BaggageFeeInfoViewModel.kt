package com.expedia.vm

import rx.subjects.PublishSubject

class BaggageFeeInfoViewModel() {
    val baggageFeeURLObserver = PublishSubject.create<String>()
}