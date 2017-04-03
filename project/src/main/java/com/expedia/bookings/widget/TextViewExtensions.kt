package com.expedia.bookings.widget

import android.widget.TextView
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isMaterialFormsEnabled

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

fun TextView.setMaterialFormsError(isValid: Boolean, errorMessage: String, rightDrawableId: Int) {
    val rightDrawable = if (rightDrawableId != 0) ContextCompat.getDrawable(this.context, rightDrawableId) else null
    val compounds = this.compoundDrawables
    this.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], rightDrawable, compounds[3])
    (this.parent as TextInputLayout).isErrorEnabled = !isValid

    if (!isValid) {
        (this.parent as TextInputLayout).error = errorMessage
    } else {
        (this.parent as TextInputLayout).error = null
        if (this.text.isBlank()) this.text = ""
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && this.paddingBottom != 8) {
        this.updatePaddingForOldApi()
    }
}

fun TextView.updatePaddingForOldApi() {
    val bottomPadding = context.resources.getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
    this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, bottomPadding)
}


class TextViewExtensions {
    companion object {
        fun setTextColorBasedOnPosition(tv: TextView, currentPosition: Int, position: Int) {
            val context = tv.context
            if (isMaterialFormsEnabled()) {
                var textColor = ContextCompat.getColor(context, R.color.default_text_color)
                if (currentPosition == position ) {
                    textColor = ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color))
                }
                tv.setTextColor(textColor)
            }
        }
    }
}
