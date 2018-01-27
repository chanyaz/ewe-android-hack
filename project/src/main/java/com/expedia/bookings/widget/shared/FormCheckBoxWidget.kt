package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import kotlinx.android.synthetic.main.form_check_box_widget.view.*

class FormCheckBoxWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.form_check_box_widget, this)
        form_checkbox.setOnClickListener {
            this.callOnClick()
        }
    }

    fun getIsChecked(): Boolean {
        return form_checkbox.isChecked
    }

    fun setIsChecked(check: Boolean) {
        form_checkbox.isChecked = check
    }

    fun setIsEnabled(check: Boolean) {
        form_checkbox.isEnabled = check
    }

    fun setText(text: String) {
        form_textbox.text = text
    }
}
