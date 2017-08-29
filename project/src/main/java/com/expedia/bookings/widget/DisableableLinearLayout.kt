package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

class DisableableLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else 0.3f
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }
}