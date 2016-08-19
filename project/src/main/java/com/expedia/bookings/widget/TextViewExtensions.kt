package com.expedia.bookings.widget

import android.widget.TextView
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui

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
