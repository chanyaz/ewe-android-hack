package com.expedia.bookings.hotel.widget.viewholder

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class Earn2xCardViewHolder(root: View) : SlimCardViewHolder(root) {

    init {
        // The header view has a bottom margin of 8dp. Add extra top margin to make it match the bottom margin of 12dp.
        itemView.setPadding(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, root.context.resources.displayMetrics).toInt(), 0, 0)
        title.text = title.context.getString(R.string.hotel_2x_points_search_title)
        subtitle.text = subtitle.context.getString(R.string.hotel_2x_points_search_content)
        icon.visibility = View.GONE
    }

    companion object {
        fun create(parent: ViewGroup): SlimCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_slim_card, parent, false)
            return Earn2xCardViewHolder(view)
        }
    }
}
