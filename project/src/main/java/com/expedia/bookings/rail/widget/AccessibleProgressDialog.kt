package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.DeprecatedProgressDialog

class AccessibleProgressDialog(context: Context) : DeprecatedProgressDialog(context) {
    init {
        setCancelable(false)
        isIndeterminate = true
    }

    fun show(accessibleText: String) {
        show()
        setContentView(R.layout.process_dialog_layout)
        val dialogView = findViewById<View>(R.id.progress_dialog_container)
        AccessibilityUtil.delayedFocusToView(dialogView, 0)
        dialogView.contentDescription = accessibleText
    }
}
