package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class HotelSearchFloatingActionPill(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val toggleViewButton by bindView<TextView>(R.id.fap_toggle_view_button)
    val filterButton by bindView<TextView>(R.id.fap_filter_button)
    var showMap = false
        private set
    private val filterCountText by bindView<TextView>(R.id.fap_filter_number_text)
    private val filterIcon by bindView<ImageView>(R.id.fap_filter_icon)
    private val toggleIcon by bindView<ImageView>(R.id.fap_toggle_icon)

    init {
        View.inflate(context, R.layout.hotel_search_floating_action_pill_layout, this)
        filterIcon.setColorFilter(ContextCompat.getColor(context, R.color.app_primary))
        toggleIcon.setColorFilter(ContextCompat.getColor(context, R.color.app_primary))
        setFilterCount(0)
        setToggleState(true)
    }

    fun setToggleState(showMap: Boolean) {
        this.showMap = showMap
        val toggleText = if (showMap) R.string.hotel_results_map_button else R.string.hotel_results_list_button
        toggleViewButton.text = context.getString(toggleText)
        toggleIcon.setImageResource(if (showMap) R.drawable.fab_map else R.drawable.fab_list)
        setToggleAccessibility()
    }

    fun setFilterCount(count: Int) {
        if (count == 0) {
            filterIcon.visibility = View.VISIBLE
            filterCountText.visibility = View.GONE
        } else {
            filterCountText.text = count.toString()
            filterCountText.visibility = View.VISIBLE
            filterIcon.visibility = View.GONE
        }
        setFilterAccessibility(count)
    }

    private fun setToggleAccessibility() {
        val contentDescriptionRes = if (showMap) R.string.show_map else R.string.show_list
        val contentDescription = context.getString(contentDescriptionRes)
        AccessibilityUtil.appendRoleContDesc(toggleViewButton, contentDescription, R.string.accessibility_cont_desc_role_button)
    }

    private fun setFilterAccessibility(count: Int) {
        val contDescBuilder = StringBuilder()
        if (count > 0) {
            val prefix = Phrase.from(context.resources.getQuantityString(R.plurals.number_results_announcement_text_TEMPLATE, count)).put("number", count).format().toString()
            contDescBuilder.append(prefix).append(". ")
        }
        contDescBuilder.append(resources.getString(R.string.hotel_results_filters_button))
        AccessibilityUtil.appendRoleContDesc(filterButton, contDescBuilder.toString(), R.string.accessibility_cont_desc_role_button)
    }
}
