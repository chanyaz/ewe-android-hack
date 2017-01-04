package com.expedia.bookings.rail.widget

import android.app.ProgressDialog
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil

class AccessibleProgressDialog(context: Context) : ProgressDialog(context) {
    init {
        setCancelable(false)
        isIndeterminate = true
    }

    fun show(accessibleText: String) {
        show()
        setContentView(R.layout.process_dialog_layout)
        val dialogView = findViewById(R.id.progress_dialog_container)
        AccessibilityUtil.delayedFocusToView(dialogView, 0)
        dialogView.contentDescription = accessibleText
    }
}