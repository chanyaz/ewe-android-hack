package com.expedia.util

import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ToggleButton
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.getParentTextInputLayout
import com.expedia.bookings.widget.setParentTextInputLayoutError
import com.expedia.bookings.widget.setRightDrawable
import com.expedia.bookings.widget.updatePaddingForOldApi
import com.google.android.gms.maps.GoogleMap
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

fun <T> endlessObserver(body: (T) -> Unit): Observer<T> {
    return object : Observer<T> {
        override fun onSubscribe(d: Disposable) {
            //ignore
        }

        override fun onNext(t: T) {
            body(t)
        }

        override fun onComplete() {
            throw OnErrorNotImplementedException(RuntimeException("Cannot call completed on endless observer " + body.javaClass))
        }

        override fun onError(e: Throwable) {
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
    this.setOnCheckedChangeListener { _, isChecked: Boolean ->
        observer.onNext(isChecked)
    }
}

fun RadioGroup.subscribeOnCheckChanged(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { _, isChecked: Int ->
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

fun <T : Any?> Observable<Optional<T>>.safeSubscribeOptional(observer: Observer<T>): Disposable {
    return this.subscribeObserver(object : DisposableObserver<Optional<T>>() {
        override fun onNext(t: Optional<T>) {
            t.value?.let {
                observer.onNext(it)
            }
        }

        override fun onComplete() {
            observer.onComplete()
        }

        override fun onError(e: Throwable) {
            observer.onError(e)
        }
    })
}
// Only emits non-null data
fun <T : Any?> Observable<T>.safeSubscribe(onNextFunc: (T) -> Unit): Disposable {
    return this.subscribe {
        if (it != null) {
            onNextFunc.invoke(it as T)
        }
    }
}

fun <T : Any?> Observable<Optional<T>>.safeSubscribeOptional(onNextFunc: (T) -> Unit): Disposable {
    return this.subscribe {
        it.value?.let {
            onNextFunc.invoke(it)
        }
    }
}

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

fun Observable<Boolean>.subscribeInverseVisibilityInvisible(view: View) {
    this.map { !it }.subscribeVisibilityInvisible(view)
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

fun TextView.subscribeTextChange(observer: Observer<String>): Disposable {
    return RxTextView.afterTextChangeEvents(this).map({
        it.view().text.toString()
    }).distinctUntilChanged().subscribeObserver(observer)
}

fun EditText.subscribeMaterialFormsError(observer: Observable<Boolean>, errorMessageId: Int, rightDrawableId: Int = 0) {
    observer.subscribe { hasError ->
        this.setRightDrawable(rightDrawableId)

        val errorMessage = this.context.resources.getString(errorMessageId)
        val parentTextInputLayout = this.getParentTextInputLayout()
        if (parentTextInputLayout != null) {
            this.setParentTextInputLayoutError(parentTextInputLayout, hasError, errorMessage)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && this.paddingBottom != 8) {
                this.updatePaddingForOldApi()
            }
        }
    }
}
