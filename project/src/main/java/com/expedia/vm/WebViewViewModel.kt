package com.expedia.vm

import rx.subjects.PublishSubject

open class WebViewViewModel {
    val webViewURLObservable = PublishSubject.create<String>()
}
