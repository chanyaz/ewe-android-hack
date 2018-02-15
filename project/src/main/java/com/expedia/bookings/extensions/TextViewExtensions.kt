package com.expedia.bookings.extensions

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

fun TextView.addErrorExclamation() {
    val context = this.context
    val themeErrorIcon = ContextCompat.getDrawable(context, Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
    val fallbackErrorIcon = ContextCompat.getDrawable(context, R.drawable.invalid)
    val errorIcon = themeErrorIcon ?: fallbackErrorIcon
    errorIcon.bounds = Rect(0, 0, errorIcon.intrinsicWidth, errorIcon.intrinsicHeight)
    val compounds = this.compoundDrawables
    this.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3])
}

fun TextView.removeErrorExclamation(newDrawableRight: Drawable?) {
    val drawables = this.compoundDrawables
    this.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], newDrawableRight, drawables[3])
}

fun TextView.setMaterialFormsError(isValid: Boolean, errorMessage: String, rightDrawableId: Int = 0) {
    setRightDrawable(rightDrawableId)

    val parentTextInputLayout = this.getParentTextInputLayout()
    if (parentTextInputLayout != null) {
        setParentTextInputLayoutError(parentTextInputLayout, !isValid, errorMessage)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && this.paddingBottom != 8) {
            this.updatePaddingForOldApi()
        }
    }
}

fun TextView.setRightDrawable(rightDrawableId: Int) {
    val rightDrawable = if (rightDrawableId != 0) ContextCompat.getDrawable(this.context, rightDrawableId) else null
    val compounds = this.compoundDrawables
    this.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], rightDrawable, compounds[3])
}

fun TextView.updatePaddingForOldApi() {
    val bottomPadding = context.resources.getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
    this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, bottomPadding)
}

fun TextView.setParentTextInputLayoutError(parentTextInputLayout: TextInputLayout, hasError: Boolean, errorMessage: String) {
    if (hasError) {
        parentTextInputLayout.error = errorMessage
        parentTextInputLayout.hideErrorTextViewFromHoverFocus()
    } else {
        parentTextInputLayout.error = null
    }
    parentTextInputLayout.isErrorEnabled = hasError
}

fun TextView.getParentTextInputLayout(): TextInputLayout? {
    return getTextInputLayoutParent(this)
}

private fun getTextInputLayoutParent(view: View): TextInputLayout? {

    if (view.parent is View) {
        if (view.parent is TextInputLayout) {
            return view.parent as TextInputLayout
        } else {
            return getTextInputLayoutParent(view.parent as View)
        }
    } else {
        return null
    }
}

fun TextView.setInverseVisibility(forward: Boolean) {
    this.visibility = if (!forward && this.text.isNotEmpty()) View.VISIBLE else View.GONE
}

fun TextView.setTextAndVisibility(text: CharSequence?) {
    this.text = text ?: ""
    setInverseVisibility(text.isNullOrBlank())
}

fun TextView.subscribeTextChange(observer: Observer<String>): Disposable {
    return RxTextView.afterTextChangeEvents(this).map({
        it.view().text.toString()
    }).distinctUntilChanged().subscribeObserver(observer)
}

class TextViewExtensions {
    companion object {
        fun setTextColorBasedOnPosition(tv: TextView, currentPosition: Int, position: Int) {
            val context = tv.context
            var textColor = ContextCompat.getColor(context, R.color.default_text_color)
            if (currentPosition == position) {
                textColor = ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color))
            }
            tv.setTextColor(textColor)
        }
    }
}
