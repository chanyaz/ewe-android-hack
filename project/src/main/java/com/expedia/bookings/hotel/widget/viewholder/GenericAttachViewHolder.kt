package com.expedia.bookings.hotel.widget.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class GenericAttachViewHolder(root: View) : SlimCardViewHolder(root) {

    init {
        title.text = title.context.getString(R.string.generic_attach_slim_card_title)
        subtitle.text = subtitle.context.getString(R.string.generic_attach_slim_card_content)

        val drawable = root.context.getDrawable(R.drawable.ic_generic_attach_with_background)
        icon.setImageDrawable(drawable)
        icon.visibility = View.VISIBLE
    }

    companion object {
        fun create(parent: ViewGroup): SlimCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_slim_card, parent, false)
            return GenericAttachViewHolder(view)
        }
    }
}
