package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.Spinner
import com.expedia.bookings.R
import kotlin.properties.Delegates

class AccessibleSpinner(context: Context, attributeSet: AttributeSet) : Spinner(context, attributeSet) {
    var valid: Boolean = true
    var spinnerLabel by Delegates.notNull<CharSequence>()

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.AccessibleSpinner, 0, 0)
        spinnerLabel = typedArray.getText(R.styleable.AccessibleSpinner_spinner_label)
        typedArray.recycle()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        //val text = this.selectedItem as String
        val error = context.resources.getString(R.string.accessibility_cont_desc_role_error)
        val sb: StringBuilder = StringBuilder(spinnerLabel)
        if (!valid) {
            sb.append(", $error")
        }
        info.text = sb.toString()
    }
}
