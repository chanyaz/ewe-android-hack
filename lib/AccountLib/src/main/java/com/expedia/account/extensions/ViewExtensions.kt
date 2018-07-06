package com.expedia.account.extensions

import android.view.View
import io.reactivex.Observer

fun View.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnClickListener {
        observer.onNext(Unit)
    }
}
