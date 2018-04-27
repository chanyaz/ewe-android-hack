package com.expedia.vm

import io.reactivex.subjects.PublishSubject

open class WebViewViewModel {
    val webViewURLObservable = PublishSubject.create<String>()
    val blankViewObservable = PublishSubject.create<Unit>()
    val backObservable = PublishSubject.create<Unit>()
    val showWebViewObservable = PublishSubject.create<Boolean>()
    val showNativeSearchObservable = PublishSubject.create<Unit>()
}
