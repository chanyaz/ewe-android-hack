package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R

class AccessibleEditTextForSpinner(context: Context, attributeSet: AttributeSet) : EditText(context, attributeSet) {
    var valid: Boolean = true
    var errorMessage = ""
    val defaultErrorString = context.resources.getString(R.string.accessibility_cont_desc_role_error)

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        val text = this.text.toString()
        val hint = (this.parent as? TextInputLayout)?.hint ?: this.hint?.toString() ?: ""
        val error = (this.parent as? TextInputLayout)?.error ?: errorMessage
        val openDialogHint = context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
        val sb: StringBuilder = StringBuilder("$hint")
        if (!text.isEmpty()) {
            sb.append(", $text, $openDialogHint")
        }
        if (!valid) {
            sb.append(", $openDialogHint, $defaultErrorString, $error")
        }
        info.text = sb.toString()
    }
}
