package com.expedia.bookings.widget

import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.vm.launch.LaunchScreenHotelAttachViewModel

class LaunchScreenAddOnHotMIPCard(itemView: View) : RecyclerView.ViewHolder(itemView), HotelAttachCardViewHolder {
    val title: AppCompatTextView by bindView(R.id.title)

    override fun bind(vm: LaunchScreenHotelAttachViewModel) {
        title.text = vm.addOnTitle
        itemView.contentDescription = vm.addOnContentDescription

        FontCache.setTypeface(title, FontCache.Font.ROBOTO_MEDIUM)
        itemView.subscribeOnClick(vm.onClickObserver)
    }
}
