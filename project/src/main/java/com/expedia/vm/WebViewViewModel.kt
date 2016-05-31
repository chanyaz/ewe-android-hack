package com.expedia.vm

import rx.subjects.PublishSubject

class WebViewViewModel() {
    val webViewURLObservable = PublishSubject.create<String>()
}
