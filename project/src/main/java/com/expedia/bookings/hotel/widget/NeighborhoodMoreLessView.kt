package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView

class NeighborhoodMoreLessView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val neighborhoodMoreLessLabel: TextView by bindView(R.id.show_more_less_text)
    private val neighborhoodMoreLessIcon: ImageButton by bindView(R.id.show_more_less_icon)

    init {
        View.inflate(context, R.layout.neighborhood_more_less_container, this)
    }

    fun showMore() {
        AnimUtils.reverseRotate(neighborhoodMoreLessIcon)
        neighborhoodMoreLessLabel.text = resources.getString(R.string.show_more)
        contentDescription = resources.getString(R.string.hotels_filter_show_more_cont_desc)
    }

    fun showLess() {
        AnimUtils.rotate(neighborhoodMoreLessIcon)
        neighborhoodMoreLessLabel.text = resources.getString(R.string.show_less)
        contentDescription = resources.getString(R.string.hotels_filter_show_less_cont_desc)
    }
}
