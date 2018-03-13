package com.expedia.bookings.itin.common

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class ItinLinkOffCardView(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    private val icon: ImageView by bindView(R.id.link_off_card_icon)
    @VisibleForTesting val heading: TextView by bindView(R.id.link_off_card_heading)
    @VisibleForTesting val subheading: TextView by bindView(R.id.link_off_card_subheading)

    init {
        View.inflate(context, R.layout.widget_itin_link_off_card_view, this)
    }

    fun setIcon(id: Int) {
        icon.setImageResource(id)
    }

    fun setHeadingText (text: CharSequence) {
        heading.text = text
    }

    fun setSubHeadingText (text: CharSequence) {
        subheading.text = text
    }

    fun hideSubheading () {
        subheading.visibility = View.GONE
        heading.minLines = 2
        heading.gravity = Gravity.CENTER_VERTICAL
    }

    fun getSubheadingVisibility(): Int {
        return subheading.visibility
    }

    fun getSubHeadingText(): CharSequence? {
        return subheading.text
    }

    fun getHeadingText(): CharSequence? {
        return heading.text
    }

    fun wrapSubHeading() {
        subheading.maxLines = 1
        subheading.ellipsize = TextUtils.TruncateAt.END
    }
}
