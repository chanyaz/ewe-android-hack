package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.bookings.widget.ContactDetailsCompletenessStatusImageView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

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

    fun setTravelerCardContentDescription(status: ContactDetailsCompletenessStatus, title: String, subtitle: String) {
        if (ContactDetailsCompletenessStatus.INCOMPLETE == status) {
            this.contentDescription = Phrase.from(context, R.string.traveler_details_incomplete_cont_desc_TEMPLATE)
                    .put("title", title)
                    .format()
                    .toString()
        } else if (ContactDetailsCompletenessStatus.COMPLETE == status) {
            this.contentDescription = Phrase.from(context, R.string.traveler_details_complete_cont_desc_TEMPLATE)
                    .put("title", title)
                    .put("subtitle", subtitle)
                    .format()
                    .toString()
        } else {
            AccessibilityUtil.appendRoleContDesc(this, title, R.string.accessibility_cont_desc_role_button)
        }
    }
}
