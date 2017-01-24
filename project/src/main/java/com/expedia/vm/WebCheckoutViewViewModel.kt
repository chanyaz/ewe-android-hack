package com.expedia.vm

import rx.subjects.PublishSubject

class WebCheckoutViewViewModel : WebViewViewModel() {
    val closeView = PublishSubject.create<Unit>()
}
