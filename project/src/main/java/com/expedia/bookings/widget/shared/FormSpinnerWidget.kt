package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.expedia.bookings.R
import kotlinx.android.synthetic.main.form_spinner_widget.view.*

class FormSpinnerWidget(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.form_spinner_widget, this)
    }

    fun setSpinnerList(list: List<String>) {
        status_spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, list)
    }

    fun getSelectedItem(): String = status_spinner.selectedItem.toString()

    fun setText(text: String) {
        status_spinner_text.text = text
    }
}