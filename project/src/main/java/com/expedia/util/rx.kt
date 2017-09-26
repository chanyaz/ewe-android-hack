package com.expedia.util

import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.getParentTextInputLayout
import com.expedia.bookings.widget.updatePaddingForOldApi
import com.google.android.gms.maps.GoogleMap
import com.jakewharton.rxbinding.widget.RxTextView
import rx.Observable
import rx.Observer
import rx.Subscription
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

fun RadioGroup.subscribeOnCheckChanged(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { radioGroup: RadioGroup, isChecked: Int ->
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

// Only emits non-null data
fun <T : Any?> Observable<T>.safeSubscribe(observer: Observer<T>): Subscription {
    return this.subscribe(object : Observer<T> {
        override fun onNext(t: T) {
            if (t != null) {
                observer.onNext(t)
            }
        }

        override fun onCompleted() {
            observer.onCompleted()
        }

        override fun onError(e: Throwable?) {
            observer.onError(e)
        }
    })
}

fun <T : Any?> Observable<Optional<T>>.safeSubscribeOptional(observer: Observer<T>): Subscription {
    return this.subscribe(object : Observer<Optional<T>> {
        override fun onNext(t: Optional<T>) {
            if (t.value != null) {
                observer.onNext(t.value)
            }
        }

        override fun onCompleted() {
            observer.onCompleted()
        }

        override fun onError(e: Throwable?) {
            observer.onError(e)
        }
    })
}
// Only emits non-null data
fun <T : Any?> Observable<T>.safeSubscribe(onNextFunc: (T) -> Unit): Subscription {
    return this.subscribe {
        if (it != null) {
            onNextFunc.invoke(it as T)
        }
    }
}

fun <T : Any?> Observable<Optional<T>>.safeSubscribeOptional(onNextFunc: (T) -> Unit): Subscription {
    return this.map { it.value }.subscribe {
        if (it != null) {
            onNextFunc.invoke(it as T)
        }
    }
}

fun Observable<FontCache.Font>.subscribeFont(textview: TextView?) {
    this.subscribe { font ->
        FontCache.setTypeface(textview, font)
    }
}

fun <T : CharSequence> Observable<T>.subscribeText(textview: TextView?): Subscription {
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

fun <T : CharSequence?> Observable<T>.subscribeTextNotBlankVisibility(view: View) {
    this.map { it?.toString()?.isNotBlank() ?: false }.subscribeVisibility(view)
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

fun TextView.subscribeTextChange(observer: Observer<String>): Subscription {
    return RxTextView.afterTextChangeEvents(this).map({
        it.view().text.toString()
    }).distinctUntilChanged().subscribe(observer)
}

fun EditText.subscribeMaterialFormsError(observer: Observable<Boolean>, errorMessageId: Int, rightDrawableId: Int = 0) {
    observer.subscribe { hasError ->
        val errorMessage = this.context.resources.getString(errorMessageId)
        val rightDrawable = if (rightDrawableId != 0) ContextCompat.getDrawable(this.context, rightDrawableId) else null
        val compounds = this.compoundDrawables
        this.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], rightDrawable, compounds[3])

        val parentTextInputLayout = this.getParentTextInputLayout() ?: return@subscribe
        if (hasError) {
            parentTextInputLayout.error = errorMessage
        } else {
            parentTextInputLayout.error = null
        }
        parentTextInputLayout.isErrorEnabled = hasError
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && this.paddingBottom != 8) {
            this.updatePaddingForOldApi()
        }
    }
}
