package com.expedia.bookings.hotel.widget.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

open class SlimCardViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    protected val title: TextView by bindView(R.id.title)
    protected val subtitle: TextView by bindView(R.id.subtitle)
    protected val icon: ImageView by bindView(R.id.icon)
}
