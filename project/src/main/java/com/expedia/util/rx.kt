package com.expedia.util

import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import rx.subjects.Subject

public fun <T> endlessObserver(body: (in T) -> Unit) : Observer<T> {
    return object : Observer<T> {
        override fun onNext(t: T) {
            body(t)
        }

        override fun onCompleted() {
            // ignore - it is endless afterall
        }

        override fun onError(e: Throwable?) {
            Log.e("I wasn't expecting an error", e)
        }
    }
}

public fun View.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnClickListener {
        observer.onNext(Unit)
    }
}

public fun Observable<String>.subscribe(textview: TextView) {
    this.subscribe { text -> textview.setText(text) }
}

public fun Observable<String>.subscribe(togglebutton: ToggleButton) {
    this.subscribe { text ->
        togglebutton.setText(text)
        togglebutton.setTextOn(text)
        togglebutton.setTextOff(text)
    }
}
