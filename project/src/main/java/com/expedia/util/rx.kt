package com.expedia.util

import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject

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

public fun View.publishOnClick(publishSubject: PublishSubject<Unit>) {
    this.setOnClickListener {
        publishSubject.onNext(Unit)
    }
}

public fun RatingBar.subscribeOnTouch(observer: Observer<Unit>) {
    this.setOnTouchListener { view, event ->
        if (event.getAction() == MotionEvent.ACTION_UP) {
            observer.onNext(Unit)
        }
        true
    }
}

public fun RadioGroup.subscribeOnCheckedChange(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { radioGroup, checkedId ->
        observer.onNext(checkedId)
    }
}

public fun RadioGroup.unsubscribeOnCheckedChange() {
    this.setOnCheckedChangeListener { radioGroup, checkedId ->
        null
    }
}

public fun CheckBox.subscribeOnCheckChanged(observer: Observer<Boolean>) {
    this.setOnClickListener {
        observer.onNext(this.isChecked())
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