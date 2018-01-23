package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ContactDetailsCompletenessStatusImageView
import com.expedia.bookings.widget.TextView

open class TravelerDetailsCard(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val detailsText: TextView by bindView(R.id.primary_details_text)
    val secondaryText: TextView by bindView(R.id.secondary_details_text)
    val travelerStatusIcon: ContactDetailsCompletenessStatusImageView by bindView(R.id.traveler_status_icon)

    init {
        val padding = resources.getDimensionPixelSize(R.dimen.traveler_select_item_padding)
        setPadding(padding, padding, padding, padding)
        orientation = HORIZONTAL
        View.inflate(context, R.layout.traveler_details_card, this)
    }
}
