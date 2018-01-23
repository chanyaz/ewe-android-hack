package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.HotelSuggestionViewModel

class HotelSuggestionViewHolder(val root: ViewGroup, private val vm: HotelSuggestionViewModel) : RecyclerView.ViewHolder(root) {
    val title: TextView by bindView(R.id.title_textview)
    val icon: ImageView by bindView(R.id.icon_imageview)
    val hierarchyIcon: ImageView by bindView(R.id.hierarchy_imageview)

    fun bind(suggestion: SuggestionV4) {
        vm.bind(suggestion)
        title.text = vm.getTitle()
        icon.setImageResource(vm.getIcon())
        if (vm.isChild() && !vm.isHistoryItem()) {
            hierarchyIcon.visibility = View.VISIBLE
            icon.visibility = View.GONE
        } else {
            hierarchyIcon.visibility = View.GONE
            icon.visibility = View.VISIBLE
        }
    }
}
