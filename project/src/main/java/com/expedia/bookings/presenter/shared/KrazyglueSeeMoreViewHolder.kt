package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.TextView
import com.expedia.vm.KrazyglueHotelSeeMoreHolderViewModel
import rx.subjects.BehaviorSubject

class KrazyglueSeeMoreViewHolder(itemView: View, context: Context, searchParams: BehaviorSubject<HotelSearchParams>, regionId: BehaviorSubject<String>) : RecyclerView.ViewHolder(itemView) {

    val offerText: TextView by bindView(R.id.hotel_offer_expire)

    val viewModel: KrazyglueHotelSeeMoreHolderViewModel by lazy {
        KrazyglueHotelSeeMoreHolderViewModel(context, searchParams.value.checkIn.toDateTimeAtCurrentTime())
    }

    init {
        offerText.text = viewModel.getOfferValidDate()
        itemView.setOnClickListener {
            FlightsV2Tracking.trackKrazyglueSeeMoreClicked()
            val flags = NavUtils.FLAG_PINNED_SEARCH_RESULTS or NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK
            searchParams.value.origin?.gaiaId = regionId.value
            HotelNavUtils.goToHotelsV2Params(it.context, searchParams.value, null, flags)
        }
    }

}

