package com.expedia.bookings.itin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class ItinMapMarkerCard (context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    private val image: ImageView by bindView(R.id.marker_image)
    private val title: TextView by bindView(R.id.marker_title)
    private val body: TextView by bindView(R.id.marker_body)
    private val chev: ImageView by bindView(R.id.card_chevron)

    init {
        View.inflate(context, R.layout.hotel_itin_map_card, this)
    }

    fun setImage(url: String) {
        if (url.isNotBlank()) {
            image.visibility = View.VISIBLE
            PicassoHelper.Builder(image)
                    .build()
                    .load(url)
        }
    }

    fun setTitle(titleText: String?) {
        title.text = titleText
    }

    fun setBody(bodyText: String?) {
        body.text = bodyText
    }

    fun hideChev(bool: Boolean) = if (bool) {
        chev.visibility = View.INVISIBLE
    } else {
        chev.visibility = View.VISIBLE
    }

    fun hideImage(bool: Boolean) = if (bool) {
        image.visibility = View.INVISIBLE
    }
    else {
        chev.visibility = View.VISIBLE
    }
}