package com.expedia.util

import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.util.operators.OperatorDistinctUntilChangedWithComparer
import com.google.android.gms.maps.GoogleMap
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.internal.operators.OperatorWithLatestFromWithMultipleArguments
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject

fun <T> endlessObserver(body: (T) -> Unit): Observer<T> {
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

fun View.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnClickListener {
        observer.onNext(Unit)
    }
}

fun GoogleMap.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnMapClickListener {
        observer.onNext(Unit)
    }
}

fun CompoundButton.subscribeOnClick(observer: Observer<Boolean>) {
    this.setOnClickListener {
        observer.onNext(this.isChecked)
    }
}

fun CompoundButton.subscribeOnCheckChanged(observer: Observer<Boolean>) {
    this.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
        observer.onNext(isChecked)
    }
}

fun View.unsubscribeOnClick() {
    this.setOnClickListener(null)
}

fun View.publishOnClick(publishSubject: PublishSubject<Unit>) {
    this.setOnClickListener {
        publishSubject.onNext(Unit)
    }
}

fun Observable<FontCache.Font>.subscribeFont(textview: TextView?) {
    this.subscribe { font ->
        FontCache.setTypeface(textview, font)
    }
}

fun <T : CharSequence> Observable<T>.subscribeText(textview: TextView?) {
    this.subscribe { textview?.text = it }
}

fun <T : CharSequence> Observable<T>.subscribeEditText(edittext: EditText?) {
    this.subscribe { text ->
        edittext?.setText(text)
        val selection = edittext?.text?.length ?: 0
        edittext?.setSelection(selection)
    }
}

fun Observable<Int>.subscribeTextColor(textview: TextView) {
    this.subscribe { textview.setTextColor(it) }
}

fun <T : CharSequence> Observable<T>.subscribeTextAndVisibility(textview: TextView) {
    this.subscribe {
        textview.text = it
    }
    this.map { it.toString().isNotBlank() }.subscribeVisibility(textview)
}

fun <T : CharSequence> Observable<T>.subscribeTextAndVisibilityInvisible(textview: TextView) {
    this.subscribe {
        textview.text = it
    }
    this.map { it.toString().isNotBlank() }.subscribeVisibilityInvisible(textview)
}

fun Observable<Drawable>.subscribeImageDrawable(imageView: ImageView?) {
    this.subscribe { drawable -> imageView?.setImageDrawable(drawable) }
}

fun Observable<Int>.subscribeBackgroundColor(view: View) {
    this.subscribe { color -> view.setBackgroundColor(color) }
}

fun Observable<Drawable?>.subscribeBackground(view: View) {
    this.subscribe { drawable -> view.background = drawable }
}

fun Observable<Float>.subscribeRating(ratingBar: RatingBar) {
    this.subscribe { ratingBar.rating = it }
}

fun Observable<Float>.subscribeRating(ratingBar: StarRatingBar) {
    this.subscribe { ratingBar.setRating(it) }
}

fun Observable<Int>.subscribeBackgroundResource(view: View) {
    this.subscribe { drawable -> view.setBackgroundResource(drawable) }
}

fun Observable<CharSequence>.subscribeToggleButton(togglebutton: ToggleButton) {
    this.subscribe { text ->
        togglebutton.text = text
        togglebutton.textOn = text
        togglebutton.textOff = text
    }
}

fun Observable<Boolean>.subscribeVisibility(view: View?): Subscription {
    return this.subscribe { visible ->
        view?.visibility = if (visible) View.VISIBLE else View.GONE
    }
}

fun Observable<Boolean>.subscribeVisibilityInvisible(view: View) {
    this.subscribe { visible ->
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }
}

fun Observable<Boolean>.subscribeInverseVisibility(view: View) {
    this.map { !it }.subscribeVisibility(view)
}

fun Observable<Boolean>.subscribeVisibility(drawable: Drawable, inverse: Boolean) {
    this.subscribe { visible ->
        drawable.mutate().alpha = if (if (inverse) !visible else visible) 255 else 0
    }
}

fun Observable<Int>.subscribeStarColor(starRatingBar: StarRatingBar) {
    this.subscribe { starRatingBar.setStarColor(it) }
}

fun Observable<Float>.subscribeStarRating(starRatingBar: StarRatingBar) {
    this.subscribe { starRatingBar.setRating(it) }
}

fun Observable<ColorMatrixColorFilter?>.subscribeGalleryColorFilter(recyclerGallery: RecyclerGallery) {
    this.subscribe { recyclerGallery.setColorFilter(it) }
}

fun Observable<ColorMatrixColorFilter?>.subscribeColorFilter(imageView: ImageView) {
    this.subscribe { imageView.colorFilter = it }
}

fun Observable<Boolean>.subscribeEnabled(view: View) {
    this.subscribe { view.isEnabled = it }
}

fun Observable<Boolean>.subscribeChecked(compoundButton: CompoundButton) {
    this.subscribe { compoundButton.isChecked = it }
}

fun Observable<Boolean>.subscribeCursorVisible(textView: TextView) {
    this.subscribe { textView.isCursorVisible = it }
}

/**
 *  Returns an observable sequence that contains only distinct contiguous elements according to the comparer.
 *
 *  var obs = observable.distinctUntilChanged{ x, y-> x == y };
 *
 * @param {Function} [comparer] Equality comparer for computed key values. If not provided, defaults to an equality comparer function.
 * @returns {Observable} An observable sequence only containing the distinct contiguous elements, based on a computed key value, from the source sequence.
 */
fun <T> Observable<T>.distinctUntilChanged(comparer: (T?, T?) -> Boolean): Observable<T> {
    return lift(OperatorDistinctUntilChangedWithComparer(comparer))
}

fun <T, A, B, C, R> Observable<T>.withLatestFrom(other1: Observable<A>, other2: Observable<B>, other3: Observable<C>, resultSelector: (T, A, B, C) -> R): Observable<R> {
    return lift(OperatorWithLatestFromWithMultipleArguments<T, A, B, C, R>(other1, other2, other3, resultSelector))
}