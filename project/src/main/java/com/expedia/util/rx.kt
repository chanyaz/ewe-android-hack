package com.expedia.util

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.*
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

public fun <T> endlessObserver(body: (in T) -> Unit) : Observer<T> {
    return object : Observer<T> {
        override fun onNext(t: T) {
            body(t)
        }

        override fun onCompleted() {
            throw OnErrorNotImplementedException(RuntimeException("Cannot call completed on endless observer " + body.javaClass))
        }

        override fun onError(e: Throwable?) {
            throw OnErrorNotImplementedException("Error at " + body.javaClass, e)
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

public fun Observable<Drawable?>.subscribe(imageView: ImageView) {
    this.subscribe { drawable -> imageView.setImageDrawable(drawable) }
}

public fun Observable<Int>.subscribeBackgroundColor(view: View) {
    this.subscribe { color -> view.setBackgroundColor(color) }
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