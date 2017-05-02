package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.launch.ActiveItinType
import com.expedia.vm.launch.ActiveItinViewModel

class ItinLaunchCard(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLine: TextView by bindView(R.id.first_line)
    val secondLine: TextView by bindView(R.id.second_line)

    private var itinType = ActiveItinType.GUEST

    init {
        itemView.setOnClickListener {
            NavUtils.goToItin(context)
            if (itinType == ActiveItinType.SIGNED_IN) {
                OmnitureTracking.trackLaunchActiveItin()
            } else {
                OmnitureTracking.trackLaunchGuestItin()
            }
        }
    }

    fun bind(context: Context, vm: ActiveItinViewModel) {
        firstLine.text = vm.firstLine
        secondLine.text = vm.secondLine
        AccessibilityUtil.appendRoleContDesc(secondLine, secondLine.text.toString(), R.string.accessibility_cont_desc_role_button)

        itinType = vm.type
    }
}
