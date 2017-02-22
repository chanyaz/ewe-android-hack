package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.ActiveItinViewModel

class ActiveItinLaunchCard(itemView: View, context: Context): RecyclerView.ViewHolder(itemView) {
    val firstLine : TextView by bindView(R.id.first_line)
    val secondLine : TextView by bindView(R.id.second_line)
    var itinId : String? = null
    var completeText = StringBuilder()

    init {
        itemView.setOnClickListener {
            NavUtils.goToItin(context, itinId )
            OmnitureTracking.trackLaunchActiveItin()
        }
    }

    fun bind(id: String, vm: ActiveItinViewModel) {
        itinId = id
        firstLine.text = vm.firstLine
        secondLine.text = vm.secondLine
        completeText.append(firstLine, secondLine)
        AccessibilityUtil.appendRoleContDesc(itemView, secondLine.text.toString(), R.string.accessibility_cont_desc_role_button)
    }
}
