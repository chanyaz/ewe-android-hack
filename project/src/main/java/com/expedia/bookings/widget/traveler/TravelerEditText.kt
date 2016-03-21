package com.expedia.bookings.widget.traveler

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui

class TravelerEditText(context: Context, attrs: AttributeSet?) : EditText(context, attrs) {
    var valid = true
    var preErrorDrawable: Drawable? = null
    val errorIcon: Drawable

    init {
        errorIcon = ContextCompat.getDrawable(context,
                Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
    }

    fun setError() {
        if (valid) {
            errorIcon.bounds = Rect(0, 0, errorIcon.intrinsicWidth, errorIcon.intrinsicHeight)
            val compounds = compoundDrawables
            if (compounds != null) {
                preErrorDrawable = compounds[2]
                setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3])
                valid = false
            }
        }
    }

    fun resetError() {
        if (!valid) {
            val compounds = compoundDrawables
            if (compounds != null) {
                setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], preErrorDrawable, compounds[3])
                valid = true
            }
        }
    }
}