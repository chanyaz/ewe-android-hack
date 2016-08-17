package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R

open class AccessibleEditText(context: Context, attributeSet: AttributeSet?) : EditText(context, attributeSet) {
    var valid: Boolean = true

    open fun getAccessibilityNodeInfo(): String {
        val text = this.text.toString()
        val hint = this.hint.toString()
        val error = context.resources.getString(R.string.accessibility_cont_desc_role_error)
        val sb: StringBuilder = StringBuilder(" $hint")
        if (!text.isEmpty()) {
            sb.append(", $text")
        }
        if (!valid) {
            sb.append(", $error")
        }
        return sb.toString()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.text = getAccessibilityNodeInfo()
    }
}
