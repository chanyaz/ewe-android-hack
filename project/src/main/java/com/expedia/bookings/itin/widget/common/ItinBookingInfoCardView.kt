package com.expedia.bookings.itin.widget.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinBookingInfoCardView(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val icon: ImageView by bindView(R.id.link_off_card_icon)
    val heading: TextView by bindView(R.id.link_off_card_heading)
    val subheading: TextView by bindView(R.id.link_off_card_subheading)

    var viewModel by notNullAndObservable<ItinBookingInfoCardViewModel> {
        setupHeading()
        setupSubheading()
        setupIcon()
        setupClickAction()
    }

    init {
        View.inflate(context, R.layout.widget_itin_link_off_card_view, this)
    }

    private fun setupHeading() {
        heading.text = viewModel.headingText
    }

    private fun setupSubheading() {
        if (!viewModel.subheadingText.isNullOrEmpty()) {
            subheading.visibility = View.VISIBLE
            subheading.text = viewModel.subheadingText
        } else {
            subheading.visibility = View.GONE
        }
    }

    private fun setupIcon() {
        icon.setImageResource(viewModel.iconImage)
    }

    private fun setupClickAction() {
        this.setOnClickListener {
            viewModel.cardClickListener()
        }
    }
}
