package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.util.AttributeSet
import com.expedia.bookings.widget.getParentTextInputLayout

class AccessiblePasswordEditText(context: Context, attributeSet: AttributeSet) : AccessibleEditText(context, attributeSet) {

    override fun getAccessibilityNodeInfo(): String {
        val hint = (this.getParentTextInputLayout() as? TextInputLayout)?.hint ?: this.hint?.toString() ?: ""
        val error = (this.getParentTextInputLayout() as? TextInputLayout)?.error ?:  errorMessage
        val sb: StringBuilder = StringBuilder("$hint")
        if (!valid) {
            sb.append(", $defaultErrorString, $error")
        }
        contentDescription = sb.toString()
        return sb.toString()
    }
}
