package com.expedia.bookings.widget.itin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelItinLinkOffCardView(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    private val icon: ImageView by bindView(R.id.link_off_card_icon)
    private val heading: TextView by bindView(R.id.link_off_card_heading)
    private val subheading: TextView by bindView(R.id.link_off_card_subheading)

    init {
        View.inflate(context, R.layout.widget_itin_link_off_card_view, this)
    }

    fun setIcon(id: Int) {
        icon.setImageResource(id)
    }

    fun setHeadingText (text: CharSequence) {
        heading.text = text
    }

    fun setSubHeadingText (text: CharSequence) {
        subheading.text = text
    }

    fun hideSubheading () {
        subheading.visibility = View.GONE
    }
}
