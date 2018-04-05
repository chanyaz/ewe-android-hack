package com.expedia.cko.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView


class HeaderWidget(context: Context?) : LinearLayout(context) {

    val headerText by bindView<TextView>(R.id.header_text)

    init {
        View.inflate(context, R.layout.header_widget, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}
