package com.expedia.bookings.presenter.shared

import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeStarRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.KrazyglueHotelViewHolderViewModel
import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.Delegates

class KrazyglueHotelViewHolder(itemView: View, searchParams: BehaviorSubject<HotelSearchParams>, regionId: BehaviorSubject<String>) : RecyclerView.ViewHolder(itemView) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name_text_view)
    val hotelGuestRating: TextView by bindView(R.id.hotel_guest_rating)
    val hotelGuestRecommend: TextView by bindView(R.id.hotel_guest_recommend)
    val hotelNoGuestRating: TextView by bindView(R.id.no_guest_rating)
    val hotelPricePerNight: TextView by bindView(R.id.hotel_price_per_night)
    val hotelImageView: ImageView by bindView(R.id.hotel_imageview)
    var hotelStarRating: StarRatingBar by Delegates.notNull()
    val hotelStrikeThroughPrice: TextView by bindView(R.id.hotel_strike_through_price)
    var trackingPosition = adapterPosition

    init {
        hotelStarRating = itemView.findViewById<StarRatingBar>(if (shouldShowCircleForRatings()) R.id.hotel_circle_rating else R.id.hotel_star_rating)
        hotelStarRating.visibility = View.VISIBLE
        hotelStarRating.setStarColor(ContextCompat.getColor(itemView.context, R.color.hotelsv2_detail_star_color))
        hotelStrikeThroughPrice.paintFlags = hotelStrikeThroughPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        itemView.setOnClickListener {
            FlightsV2Tracking.trackKrazyglueHotelClicked(trackingPosition)
            val hotelSearchParams = searchParams.value
            hotelSearchParams.origin?.gaiaId = regionId.value
            hotelSearchParams.origin?.hotelId = viewModel.hotelId
            hotelSearchParams.updateSearchDestination = true
            val flags = NavUtils.FLAG_PINNED_SEARCH_RESULTS or NavUtils.FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK
            HotelNavUtils.goToHotelsV2Params(it.context, searchParams.value, null, flags)
        }
    }

    val viewModel: KrazyglueHotelViewHolderViewModel by lazy {
        val vm = KrazyglueHotelViewHolderViewModel()
        vm.hotelImageURLObservable.subscribe { hotelImageURL ->
            if (hotelImageURL.isNotBlank()) {
                PicassoHelper.Builder(hotelImageView)
                        .setError(R.drawable.room_fallback)
                        .build()
                        .load(hotelImageURL)
            }
        }
        vm.hotelNameObservable.subscribeText(hotelNameTextView)
        vm.hotelStarRatingObservable.subscribeStarRating(hotelStarRating)
        vm.hotelStarRatingVisibilityObservable.subscribeVisibility(hotelStarRating)
        vm.hotelGuestRatingObservable.subscribeText(hotelGuestRating)
        vm.hotelGuestRatingVisibilityObservable.subscribeVisibility(hotelGuestRating)
        vm.hotelGuestRatingVisibilityObservable.subscribeVisibility(hotelGuestRecommend)
        vm.hotelGuestRatingVisibilityObservable.subscribeInverseVisibility(hotelNoGuestRating)
        vm.hotelStrikeThroughPriceObservable.subscribeTextAndVisibility(hotelStrikeThroughPrice)
        vm.hotelPricePerNightObservable.subscribeText(hotelPricePerNight)
        vm
    }
}
