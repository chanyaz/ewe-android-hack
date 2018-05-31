package com.expedia.bookings.hotel.widget.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class AddOnAttachViewHolder(root: View) : RecyclerView.ViewHolder(root) {

    private val title: TextView by bindView(R.id.title)
    private val icon: ImageView by bindView(R.id.icon)

    init {
        title.text = Phrase.from(root.context, R.string.add_on_attach_slim_card_title_TEMPLATE).put("brand", BuildConfig.brand).format().toString()

        val drawable = root.context.getDrawable(R.drawable.ic_add_on_attach_for_white_bg)
        icon.setImageDrawable(drawable)
        icon.visibility = View.VISIBLE
    }

    companion object {
        fun create(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.add_on_attach_hotel_slim_card, parent, false)
            return AddOnAttachViewHolder(view)
        }
    }
}
