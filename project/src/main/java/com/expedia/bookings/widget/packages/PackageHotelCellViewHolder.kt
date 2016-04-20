package com.expedia.bookings.widget.packages

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.HotelViewModel
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.packages.PackageHotelViewModel

class PackageHotelCellViewHolder(root: ViewGroup, width: Int) : HotelCellViewHolder(root, width) {
    val unrealDealMessageContainer: LinearLayout by root.bindView(R.id.unreal_deal_container)
    val unrealDealMessage: TextView by root.bindView(R.id.unreal_deal_message)

    val priceIncludesFlightsDivider: View by root.bindView(R.id.price_includes_flights_divider)
    val priceIncludesFlightsView: TextView by root.bindView(R.id.price_includes_flights)

    override fun bind(viewModel: HotelViewModel) {
        super.bind(viewModel)
        viewModel as PackageHotelViewModel
        viewModel.unrealDealMessageObservable.subscribeText(unrealDealMessage)
        viewModel.unrealDealMessageVisibilityObservable.subscribeVisibility(unrealDealMessageContainer)

        viewModel.priceIncludesFlightsObservable.subscribeVisibility(priceIncludesFlightsDivider)
        viewModel.priceIncludesFlightsObservable.subscribeVisibility(priceIncludesFlightsView)
    }

    override fun getGuestRatingRecommendedText(rating: Float, resources: Resources): String {
        return resources.getString(R.string.hotel_guest_recommend)
    }

    override fun getGuestRatingBackground(rating: Float, context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.user_review_background)
    }
}