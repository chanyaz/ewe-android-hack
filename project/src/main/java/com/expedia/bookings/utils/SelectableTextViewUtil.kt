package com.expedia.bookings.utils

import android.view.MotionEvent
import com.expedia.bookings.widget.TextView

object SelectableTextViewUtil {

    /**
     * For text views that are both selectable and clickable, a user must click the view twice in order to have the
     * onClickListener fire. This happens because selectText has priority over onClick. By setting the touch listener
     * to give the view focus as soon as the user touches the view, selectText won't be called, allowing the user to
     * onClick after a single click.
     */
    fun setOnClickForSelectableTextView(textView: TextView, onClickAction: () -> Unit) {
        textView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) view.requestFocus()
            false
        }
        textView.setOnClickListener {
            onClickAction()
        }
    }
}
