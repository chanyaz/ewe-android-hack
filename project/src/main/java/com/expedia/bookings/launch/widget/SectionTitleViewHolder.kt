package com.expedia.bookings.launch.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class SectionTitleViewHolder(sectionTitleView: View) : RecyclerView.ViewHolder(sectionTitleView) {
    private val title: TextView by bindView(R.id.section_title_text_view)

    fun bind(titleText: String) {
        title.text = titleText
    }
}
