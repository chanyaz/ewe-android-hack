package com.expedia.bookings.widget.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FavoriteButton
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.util.getABTestGuestRatingBackground
import com.expedia.util.getABTestGuestRatingText
import com.expedia.vm.hotel.FavoriteButtonViewModel
import com.expedia.vm.hotel.HotelViewModel
import rx.subjects.PublishSubject


class HotelCellViewHolder(root: ViewGroup, width: Int, private val hotelFavoriteChange: PublishSubject<Pair<String, Boolean>>) : AbstractHotelCellViewHolder(root, width) {

    val showFavorites = HotelFavoriteHelper.showHotelFavoriteTest(root.context)
    val heartView: FavoriteButton by root.bindView(R.id.heart_image_view)

    override fun bind(viewModel: HotelViewModel) {
        super.bind(viewModel)
        if (showFavorites) {
            val favoriteButtonViewModel = FavoriteButtonViewModel(heartView.context, hotelId, HotelTracking(), HotelTracking.PageName.SEARCH_RESULT)
            heartView.viewModel = favoriteButtonViewModel
            favoriteButtonViewModel.favoriteChangeSubject.subscribe(hotelFavoriteChange)
            heartView.updateImageState()
            heartView.bringToFront()
        }
    }

    override fun getGuestRatingBackground(rating: Float, context: Context): Drawable {
        return getABTestGuestRatingBackground(rating, context)
    }

    override fun getGuestRatingRecommendedText(rating: Float, resources: Resources): String {
        return getABTestGuestRatingText(rating, resources)
    }
}