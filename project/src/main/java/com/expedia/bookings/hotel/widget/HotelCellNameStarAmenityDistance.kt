package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.shouldShowCircleForRatings
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.hotel.vm.HotelViewModel
import kotlin.properties.Delegates

class HotelCellNameStarAmenityDistance(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name)
    val starRatingBar: StarRatingBar by bindView(R.id.star_rating_bar)
    val circleRatingBar: StarRatingBar by bindView(R.id.circle_rating_bar)
    val neighborhoodNameOrDistanceFromLocationTextView: TextView by bindView(R.id.hotel_neighborhood_or_distance_from_location)

    var ratingBar: StarRatingBar by Delegates.notNull()

    init {
        View.inflate(context, R.layout.hotel_cell_name_star_amenity_distance, this)

        val attrSet = context.obtainStyledAttributes(attrs, R.styleable.HotelCellNameStarAmenityDistance, 0, 0)
        val maxLines = attrSet.getInt(R.styleable.HotelCellNameStarAmenityDistance_hotel_name_max_line, 2)
        val textColor = attrSet.getInt(R.styleable.HotelCellNameStarAmenityDistance_hotel_name_text_color, hotelNameTextView.currentTextColor)

        hotelNameTextView.setTextColor(textColor)
        hotelNameTextView.maxLines = maxLines

        if (shouldShowCircleForRatings()) {
            ratingBar = circleRatingBar
        } else {
            ratingBar = starRatingBar
        }
        ratingBar.visibility = View.VISIBLE

        attrSet.recycle()
    }

    fun update(viewModel: HotelViewModel) {
        hotelNameTextView.text = viewModel.hotelName
        updateStarRating(viewModel)
        updateAmenityAndDistance(viewModel)
    }

    private fun updateAmenityAndDistance(viewModel: HotelViewModel) {
        if (viewModel.isShowHotelHotelDistance) {
            neighborhoodNameOrDistanceFromLocationTextView.text = viewModel.distanceFromCurrentLocation()
        } else {
            neighborhoodNameOrDistanceFromLocationTextView.text = viewModel.neighborhoodName
        }
    }

    private fun updateStarRating(viewModel: HotelViewModel) {
        ratingBar.setVisibility(viewModel.showStarRating)
        ratingBar.setRating(viewModel.hotelStarRating)
        ratingBar.setStarColor(viewModel.getStarRatingColor())
    }
}
