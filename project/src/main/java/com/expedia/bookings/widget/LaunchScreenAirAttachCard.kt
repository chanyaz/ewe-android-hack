package com.expedia.bookings.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.bindView
import com.expedia.vm.launch.LaunchScreenAirAttachViewModel

class LaunchScreenAirAttachCard(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstLineTextView: TextView by bindView(R.id.air_attach_first_line)
    val secondLineTextView: TextView by bindView(R.id.air_attach_second_line)
    val offerExpiresTextView: TextView by bindView(R.id.air_attach_offer_expires_text)

    fun bind(vm: LaunchScreenAirAttachViewModel) {
        vm.firstLineObserver.subscribeText(firstLineTextView)
        vm.secondLineObserver.subscribeText(secondLineTextView)
        vm.offerExpiresObserver.subscribeText(offerExpiresTextView)

        itemView.subscribeOnClick(vm.onClickObserver)
    }
}
