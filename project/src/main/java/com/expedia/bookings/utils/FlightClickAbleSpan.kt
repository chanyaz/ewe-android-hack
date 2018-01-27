package com.expedia.bookings.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking

class FlightClickAbleSpan(private val mContext: Context) : ClickableSpan() {

    override fun onClick(view: View) {
        val textView = view as TextView
        val spanned = textView.text as Spanned
        val start = spanned.getSpanStart(this)
        val end = spanned.getSpanEnd(this)
        val textToCopy = TextUtils.substring(spanned, start, end)
        ClipboardUtils.setText(mContext, textToCopy)
        Toast.makeText(mContext, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        OmnitureTracking.trackItinFlightCopyPNR()
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = ContextCompat.getColor(mContext, R.color.app_primary)
    }
}
