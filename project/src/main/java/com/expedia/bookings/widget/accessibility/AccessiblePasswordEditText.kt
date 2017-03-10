package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.util.AttributeSet
import com.expedia.bookings.R

class AccessiblePasswordEditText(context: Context, attributeSet: AttributeSet) : AccessibleEditText(context, attributeSet) {

    override fun getAccessibilityNodeInfo(): String {
        val hint = (this.parent as TextInputLayout).hint ?: this.hint.toString() ?: ""
        val error = context.resources.getString(R.string.accessibility_cont_desc_role_error)
        val sb: StringBuilder = StringBuilder(" $hint")
        if (!valid) {
            sb.append(", $error")
        }
        contentDescription = sb.toString()
        return sb.toString()
    }
}
