package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class LaunchTabView(context: Context, iconId: Int, text: String) : LinearLayout(context) {

    private val baseIconColor = ContextCompat.getColor(context, R.color.gray500)
    private val baseTextColor = ContextCompat.getColor(context, R.color.gray500)
    private val selectedColor = ContextCompat.getColor(context, R.color.launch_screen_selected_tab_icon)
    private val tabIcon: ImageView by bindView(R.id.tab_icon)
    private val tabText: TextView by bindView(R.id.tab_text)

    init {
        View.inflate(context, R.layout.launch_tab_view, this)

        tabIcon.setImageDrawable(context.getDrawable(iconId))
        tabIcon.setColorFilter(baseIconColor, PorterDuff.Mode.SRC_IN)

        tabText.text = text
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if (selected) {
            tabIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)
            tabText.setTextColor(selectedColor)
        } else {
            tabIcon.setColorFilter(baseIconColor, PorterDuff.Mode.SRC_ATOP)
            tabText.setTextColor(baseTextColor)
        }
    }
}
