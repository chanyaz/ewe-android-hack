package com.expedia.bookings.itin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import kotlinx.android.synthetic.main.widget_toolbar_with_spinner.view.toolbar_button
import kotlinx.android.synthetic.main.widget_toolbar_with_spinner.view.toolbar_spinner

class ToolbarWithSpinner(context: Context, attr: AttributeSet?) : Toolbar(context, attr) {

    init {
        View.inflate(context, R.layout.widget_toolbar_with_spinner, this)
        this.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        this.navigationContentDescription = context.getText(R.string.toolbar_nav_icon_cont_desc)
    }

    fun setBackOnClickListener(clickListener: OnClickListener) {
        this.setNavigationOnClickListener(clickListener)
    }

    fun setSpinnerList(list: List<String>?) {
        if (list != null && list.isNotEmpty()) {
            val adapter = ArrayAdapter<String>(context, R.layout.itin_terminal_spinner_base, list)
            adapter.setDropDownViewResource(R.layout.itin_terminal_spinner_item)
            toolbar_spinner.adapter = adapter
        }
    }

    fun setSpinnerListener(listener: AdapterView.OnItemSelectedListener) {
        toolbar_spinner.onItemSelectedListener = listener
    }

    fun setButtonListener(listener: OnClickListener) {
        toolbar_button.setOnClickListener(listener)
    }

    fun setButtonText(resID: Int) {
        toolbar_button.setText(resID)
        AccessibilityUtil.appendRoleContDesc(toolbar_button, toolbar_button.text.toString(), R.string.accessibility_cont_desc_role_button)
    }
}
