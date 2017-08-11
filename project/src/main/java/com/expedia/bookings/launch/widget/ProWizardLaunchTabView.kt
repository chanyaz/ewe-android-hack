package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class ProWizardLaunchTabView(context: Context, iconId: Int, text: String): LinearLayout(context) {

    private val baseIconColor = ContextCompat.getColor(context, R.color.gray6)
    private val baseTextColor = ContextCompat.getColor(context, R.color.gray7)
    private val selectedColor = ContextCompat.getColor(context, R.color.app_primary)

    private val textSize = context.resources.getDimensionPixelSize(R.dimen.launch_pro_wizard_tab_text_size)
    private val selectedTextSize = context.resources.getDimensionPixelSize(R.dimen.launch_pro_wizard_tab_selected_text_size)

    private val tabIcon: ImageView by bindView(R.id.tab_icon)
    private val tabText: TextView by bindView(R.id.tab_text)

    init {
        View.inflate(context, R.layout.pro_wizard_launch_tab_view, this)

        tabIcon.setImageDrawable(context.getDrawable(iconId))
        tabIcon.setColorFilter(baseIconColor, PorterDuff.Mode.SRC_IN)

        tabText.text = text
        tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        tabText.setTextColor(baseTextColor)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if (selected) {
            tabIcon.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)
            tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTextSize.toFloat())
            tabText.setTextColor(selectedColor)
        } else {
            tabIcon.setColorFilter(baseIconColor, PorterDuff.Mode.SRC_ATOP)
            tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            tabText.setTextColor(baseTextColor)
        }
    }
}
