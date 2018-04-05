package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R

class WebViewToolbar(context: Context, attr: AttributeSet?) : AbstractToolbar(context, attr) {
    override fun setNavigation() {
        this.navigationIcon = context.getDrawable(R.drawable.ic_close_white_24dp)
        this.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_cont_desc)
    }
}
