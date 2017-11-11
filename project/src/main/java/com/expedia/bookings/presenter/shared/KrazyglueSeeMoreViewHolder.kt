package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.KrazyglueHotelSeeMoreHolderViewModel
import org.joda.time.DateTime

class KrazyglueSeeMoreViewHolder(itemView: View, val context: Context, departureDate: DateTime) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val offerText: TextView by bindView(R.id.hotel_offer_expire)

    val viewModel: KrazyglueHotelSeeMoreHolderViewModel by lazy {
        KrazyglueHotelSeeMoreHolderViewModel(context, departureDate)
    }

    init {
        offerText.text = viewModel.getOfferValidDate()
    }

    override fun onClick(p0: View?) {
//        TODO: go onto hotels activity
    }
}

