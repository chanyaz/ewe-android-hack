package com.expedia.bookings.widget.packages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel
import com.expedia.vm.packages.PackageHotelViewModel

class PackageHotelCellViewHolder(root: ViewGroup, width: Int) : AbstractHotelCellViewHolder(root) {
    val unrealDealMessageContainer: LinearLayout by root.bindView(R.id.unreal_deal_container)
    val unrealDealMessage: TextView by root.bindView(R.id.unreal_deal_message)
    val priceIncludesFlightsDivider: View by root.bindView(R.id.price_includes_flights_divider)
    val priceIncludesFlightsView: TextView by root.bindView(R.id.price_includes_flights)

    init {
        bindViewModel()
    }

    fun bindViewModel() {
        viewModel as PackageHotelViewModel
        viewModel.unrealDealMessageObservable.subscribeText(unrealDealMessage)
        viewModel.unrealDealMessageVisibilityObservable.subscribeVisibility(unrealDealMessageContainer)

        viewModel.priceIncludesFlightsObservable.subscribeVisibility(priceIncludesFlightsDivider)
        viewModel.priceIncludesFlightsObservable.subscribeVisibility(priceIncludesFlightsView)
    }

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return PackageHotelViewModel(context)
    }
}