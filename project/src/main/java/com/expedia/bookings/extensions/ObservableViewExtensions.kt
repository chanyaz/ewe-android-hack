package com.expedia.bookings.extensions

import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.StarRatingBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

fun Observable<FontCache.Font>.subscribeFont(textview: TextView?) {
    this.subscribe { font ->
        FontCache.setTypeface(textview, font)
    }
}

fun <T : CharSequence> Observable<T>.subscribeText(textview: TextView?): Disposable {
    return this.subscribe { textview?.text = it }
}

fun Observable<String>.subscribeContentDescription(view: View?) {
    this.subscribe { view?.contentDescription = it }
}

fun <T : CharSequence> Observable<T>.subscribeEditText(edittext: EditText) {
    this.subscribe { text ->
        if (edittext.text?.toString() != text?.toString()) edittext.setText(text)
        edittext.setSelection(edittext.text?.length ?: 0)
    }
}

fun Observable<Int>.subscribeTextColor(textview: TextView) {
    this.subscribe { textview.setTextColor(it) }
}

fun <T : CharSequence?> Observable<T>.subscribeTextAndVisibility(textview: TextView) {
    this.subscribe {
        textview.text = it
    }
    this.map { it?.toString()?.isNotBlank() ?: false }.subscribeVisibility(textview)
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

fun Observable<Float>.subscribeRating(ratingBar: StarRatingBar) {
    this.subscribe { ratingBar.setRating(it) }
}

fun Observable<Int>.subscribeBackgroundResource(view: View) {
    this.subscribe { drawable -> view.setBackgroundResource(drawable) }
}

fun Observable<Boolean>.subscribeVisibility(view: View?): Disposable {
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

fun Observable<Boolean>.subscribeInverseVisibilityInvisible(view: View) {
    this.map { !it }.subscribeVisibilityInvisible(view)
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

fun Observable<PorterDuffColorFilter?>.subscribePorterDuffColorFilter(imageView: ImageView) {
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
