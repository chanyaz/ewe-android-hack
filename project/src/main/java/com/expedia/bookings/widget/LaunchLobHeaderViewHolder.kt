package com.expedia.bookings.widget

import android.support.v7.widget.RecyclerView

class LaunchLobHeaderViewHolder(private val lobWidget: NewLaunchLobWidget) : RecyclerView.ViewHolder(lobWidget) {

    fun onPOSChange() {
        lobWidget.onPOSChange()
    }

}
