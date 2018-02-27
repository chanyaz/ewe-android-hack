package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.hotel.HotelViewModel

class HotelCellUrgencyMessage(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val urgencyIconImageView: ImageView by bindView(R.id.urgency_icon)
    val urgencyMessageTextView: TextView by bindView(R.id.urgency_message)

    init {
        View.inflate(context, R.layout.hotel_cell_urgency_message, this)
    }

    fun update(viewModel: HotelViewModel) {
        val urgencyMessage = viewModel.getHighestPriorityUrgencyMessage()

        if (urgencyMessage != null) {
            setupUrgencyMessage(urgencyMessage)
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }

    private fun setupUrgencyMessage(urgencyMessage: HotelViewModel.UrgencyMessage) {
        if (urgencyMessage.hasIconDrawable()) {
            urgencyIconImageView.visibility = View.VISIBLE
            val urgencyIcon = ContextCompat.getDrawable(context, urgencyMessage.iconDrawableId!!)
            urgencyIcon.setColorFilter(ContextCompat.getColor(context, R.color.hotel_urgency_icon_color), PorterDuff.Mode.SRC_IN)
            urgencyIconImageView.setImageDrawable(urgencyIcon)
        } else {
            urgencyIconImageView.visibility = View.GONE
        }

        urgencyMessageTextView.text = urgencyMessage.message
        urgencyMessageTextView.setTextColor(urgencyMessage.getMessageTextColor(context))

        this.setBackgroundColor(urgencyMessage.getBackgroundColor(context))
    }
}
