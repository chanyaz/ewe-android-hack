package com.expedia.util

import android.view.View
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

public fun <T> endlessObserver(body: (in T) -> Unit) : Observer<T> {
    return object : Observer<T> {
        override fun onNext(t: T) {
            body(t)
        }

        override fun onCompleted() {
            throw OnErrorNotImplementedException(RuntimeException("Cannot call completed on endless observer"))
        }

        override fun onError(e: Throwable?) {
            throw OnErrorNotImplementedException(e)
        }
    }
}

public fun View.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnClickListener {
        observer.onNext(Unit)
    }
}

public fun RadioGroup.subscribeOnCheckedChange(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { radioGroup, checkedId ->
        observer.onNext(checkedId)
    }
}

public fun ToggleButton.subscribeOnCheckChanged(observer: Observer<Boolean>) {
    this.setOnClickListener {
        observer.onNext(this.isChecked())
    }
}

public fun <T : CharSequence> Observable<T>.subscribe(textview: TextView) {
    this.subscribe { text -> textview.setText(text) }
}

public fun Observable<Float>.subscribe(ratingBar: RatingBar) {
    this.subscribe { text -> ratingBar.setRating(text) }
}

public fun Observable<CharSequence>.subscribeToggleButton(togglebutton: ToggleButton) {
    this.subscribe { text ->
        togglebutton.setText(text)
        togglebutton.setTextOn(text)
        togglebutton.setTextOff(text)
    }
}

public fun Observable<Boolean>.subscribeVisibility(view: View) {
    this.subscribe { visible ->
        view.setVisibility(if (visible) View.VISIBLE else View.GONE)
    }
}
