package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R

class AccessibleEditTextForSpinner(context: Context, attributeSet: AttributeSet) : EditText(context, attributeSet) {
    var valid: Boolean = true

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        if (!valid) {
            val hint = this.hint.toString()
            info.text = hint + ", " + context.resources.getString(R.string.accessibility_cont_desc_opens_dialog) + ", " + context.resources.getString(R.string.accessibility_cont_desc_role_error)
        } else {
            val text = this.text.toString()
            val hint = this.hint.toString()
            if (text.isEmpty()) {
                info.text = " $hint" + ", " + context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
            } else {
                info.text = " $hint, $text" + ", " + context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
            }
        }
    }
}
