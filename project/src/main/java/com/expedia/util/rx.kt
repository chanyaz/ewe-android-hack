package com.expedia.util

import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.expedia.bookings.widget.StarRatingBar
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

public fun CompoundButton.subscribeOnCheckChanged(observer: Observer<Boolean>) {
    this.setOnClickListener {
        observer.onNext(this.isChecked)
    }
}

public fun View.unsubscribeOnClick() {
    this.setOnClickListener(null)
}

public fun View.publishOnClick(publishSubject: PublishSubject<Unit>) {
    this.setOnClickListener {
        publishSubject.onNext(Unit)
    }
}

public fun RadioGroup.subscribeOnCheckedChange(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { radioGroup, checkedId ->
        observer.onNext(checkedId)
    }
}

public fun RadioGroup.unsubscribeOnCheckedChange() {
    this.setOnCheckedChangeListener(null)
}

public fun <T : CharSequence> Observable<T>.subscribeText(textview: TextView) {
    this.subscribe { textview.text = it }
}

public fun <T : CharSequence> Observable<T>.subscribeTextAndVisibility(textview: TextView) {
    this.subscribe {
        textview.text = it
    }
    this.map { it.toString().isNotBlank() }.subscribeVisibility(textview)
}

public fun Observable<Drawable>.subscribeImageDrawable(imageView: ImageView) {
    this.subscribe { drawable -> imageView.setImageDrawable(drawable) }
}

public fun Observable<Int>.subscribeBackgroundColor(view: View) {
    this.subscribe { color -> view.setBackgroundColor(color) }
}

public fun Observable<Float>.subscribeRating(ratingBar: RatingBar) {
    this.subscribe { ratingBar.rating = it }
}

public fun Observable<Float>.subscribeRating(ratingBar: StarRatingBar) {
    this.subscribe { ratingBar.setRating(it)}
}

public fun Observable<Int>.subscribeBackgroundResource(view: View) {
    this.subscribe { drawable -> view.setBackgroundResource(drawable) }
}

public fun Observable<CharSequence>.subscribeToggleButton(togglebutton: ToggleButton) {
    this.subscribe { text ->
        togglebutton.text = text
        togglebutton.textOn = text
        togglebutton.textOff = text
    }
}

public fun Observable<Boolean>.subscribeVisibility(view: View) {
    this.subscribe { visible ->
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}

public fun Observable<Boolean>.subscribeInverseVisibility(view: View) {
    this.map { !it }.subscribeVisibility(view)
}

public fun Observable<Boolean>.subscribeVisibility(drawable: Drawable, inverse: Boolean) {
    this.subscribe { visible ->
        drawable.mutate().alpha = if (if (inverse) !visible else visible) 255 else 0
    }
}
