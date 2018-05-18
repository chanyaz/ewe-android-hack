package com.expedia.util

import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R

fun Toolbar.setShareButton() {
    this.inflateMenu(R.menu.share_menu)
    val shareMenuItem = this.menu.findItem(R.id.menu_share)
    MenuItemCompat.setContentDescription(shareMenuItem, context.getString(R.string.share_action_content_description))
    //        TODO add clickListener, add text to share message, perform share intent
}
