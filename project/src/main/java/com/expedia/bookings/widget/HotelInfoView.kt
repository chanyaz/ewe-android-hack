package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView

class HotelInfoView(context: Context) : LinearLayout(context) {

    val infoHeader: TextView by bindView(R.id.info_header)
    val infoText: TextView by bindView(R.id.info_text)

    init {
        View.inflate(getContext(), R.layout.widget_hotel_info, this)
        orientation = LinearLayout.VERTICAL
        val padding = resources.getDimensionPixelSize(R.dimen.widget_hotel_info_padding)
        setPadding(padding, 0, padding, padding)
    }

    fun setText(header: String, info: String): HotelInfoView {
        infoHeader.text = HtmlCompat.stripHtml(header)
        infoText.text = HtmlCompat.fromHtml(StrUtils.getFormattedContent(context, info))
        return this
    }
}
