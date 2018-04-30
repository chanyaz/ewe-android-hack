package com.expedia.bookings.widget.packages

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.hotel.HotelViewModel
import com.expedia.bookings.packages.vm.PackageHotelViewModel
import com.expedia.bookings.widget.shared.AbstractHotelResultCellViewHolder

class PackageHotelCellViewHolder(root: ViewGroup) : AbstractHotelResultCellViewHolder(root) {
    val unrealDealMessageContainer: LinearLayout by bindView(R.id.unreal_deal_container)
    val unrealDealMessage: TextView by bindView(R.id.unreal_deal_message)

    init {
        bindViewModel()
    }

    fun bindViewModel() {
        viewModel as PackageHotelViewModel
        viewModel.unrealDealMessageObservable.subscribeText(unrealDealMessage)
        viewModel.unrealDealMessageVisibilityObservable.subscribeVisibility(unrealDealMessageContainer)
    }

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return PackageHotelViewModel(context)
    }
}
