package com.expedia.util

import android.annotation.SuppressLint
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.expedia.bookings.R

@SuppressLint("InflateParams")
fun Toolbar.setShareButton() {
    this.inflateMenu(R.menu.share_menu)
    val shareMenuItem = this.menu.findItem(R.id.menu_share)
    val shareView = LayoutInflater.from(context).inflate(R.layout.growth_share_button, null) as LinearLayout
    shareMenuItem.actionView = shareView
    //        TODO add clickListener, add text to share message, perform share intent
}
