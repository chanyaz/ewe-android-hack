package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.User
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.ActiveItinViewModel

class ItinLaunchCard(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLine: TextView by bindView(R.id.first_line)
    val secondLine: TextView by bindView(R.id.second_line)

    init {
        itemView.setOnClickListener {
            NavUtils.goToItin(context)
            if (User.isLoggedIn(context)) {
                OmnitureTracking.trackLaunchActiveItin()
            } else {
                OmnitureTracking.trackLaunchGuestItin()
            }
        }
    }

    fun bind(context: Context, vm: ActiveItinViewModel) {
        firstLine.text = vm.firstLine
        secondLine.text = vm.secondLine
        AccessibilityUtil.appendRoleContDesc(itemView, secondLine.text.toString(), R.string.accessibility_cont_desc_role_button)
        if (User.isLoggedIn(context)) {
            AccessibilityUtil.appendRoleContDesc(itemView, secondLine.text.toString(), R.string.accessibility_cont_desc_role_button)
        } else {
            val itemViewContDesc = context.getString(R.string.launch_upcoming_trips_subtext_guest_user_cont_desc)
            AccessibilityUtil.appendRoleContDesc(itemView, itemViewContDesc.toString(), R.string.accessibility_cont_desc_role_button)
        }
    }
}
