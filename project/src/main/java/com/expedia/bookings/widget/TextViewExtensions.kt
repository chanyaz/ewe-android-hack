package com.expedia.bookings.widget

import android.widget.TextView
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
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


class TextViewExtensions {
    companion object {
        fun setTextColorBasedOnPosition(tv: TextView, currentPosition: Int, position: Int) {
            if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(tv.context, AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms,
                    R.string.preference_universal_checkout_material_forms)) {
                var textColor = ContextCompat.getColor(tv.context, R.color.default_text_color)
                if (currentPosition == position ) {
                    textColor = ContextCompat.getColor(tv.context, Ui.obtainThemeResID(tv.context, R.attr.primary_color))
                }
                tv.setTextColor(textColor)
            }
        }

        @JvmOverloads
        fun TextView.setMaterialFormsError(isValid: Boolean, errorMessage: String) {
            val compounds = this.compoundDrawables
            this.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], null, compounds[3])
            (this.parent as TextInputLayout).isErrorEnabled = !isValid

            if (!isValid) {
                (this.parent as TextInputLayout).error = errorMessage
            } else {
                (this.parent as TextInputLayout).error = null
            }
        }
    }
}
