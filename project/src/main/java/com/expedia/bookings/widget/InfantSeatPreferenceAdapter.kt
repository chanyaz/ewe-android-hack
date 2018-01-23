package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.GuestsPickerUtils

class InfantSeatPreferenceAdapter(val context: Context, val list: List<String>) : BaseAdapter() {

    val padding = context.resources.getDimensionPixelSize(R.dimen.flight_search_airport_padding_left)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createViewFromResource(position, convertView, parent, R.layout.age_spinner_item)
        val textView = view as TextView
        val icon = ContextCompat.getDrawable(parent.context, R.drawable.search_form_traveler_picker_infant).mutate()
        icon.setColorFilter(ContextCompat.getColor(parent.context, R.color.search_dialog_icon_color), PorterDuff.Mode.SRC_IN)
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
        return view
    }

    protected fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup, resource: Int): View {
        var view = convertView
        if (convertView == null) {
            view = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        }

        val text = view as TextView
        text.text = list[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.traveler_spinner_dropdown)
    }

    override fun getCount(): Int {
        return GuestsPickerUtils.MAX_SEAT_PREFERENCE
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
