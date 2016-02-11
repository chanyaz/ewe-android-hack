package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui

public class TravelerTextInput(context: Context, attrs: AttributeSet?) : MaterialTextInput(context, attrs) {
    var valid = true
    var preErrorDrawable: Drawable? = null
    val errorIcon: Drawable

    init {
        errorIcon = ContextCompat.getDrawable(context,
                Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
    }

    public fun setError() {
        if (valid) {
            errorIcon.bounds = Rect(0, 0, errorIcon.intrinsicWidth, errorIcon.intrinsicHeight)
            val compounds = editText?.compoundDrawables
            if (compounds != null) {
                preErrorDrawable = compounds[2]
                editText?.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3])
                valid = false
            }
        }
    }

    public fun resetError() {
        if (!valid) {
            val compounds = editText?.compoundDrawables
            if (compounds != null) {
                editText?.setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], preErrorDrawable, compounds[3])
                valid = true
            }
        }
    }
}