package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeStarColor
import com.expedia.util.subscribeStarRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel
import kotlin.properties.Delegates

class HotelCellNameStarAmenityDistance(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name)
    val starRatingBar: StarRatingBar by bindView(R.id.star_rating_bar)
    val circleRatingBar: StarRatingBar by bindView(R.id.circle_rating_bar)
    val amenityOrDistanceFromLocationTextView: TextView by bindView(R.id.hotel_amenity_or_distance_from_location)

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

    fun bindHotelViewModel(viewModel: HotelViewModel) {
        viewModel.hotelNameObservable.subscribeText(hotelNameTextView)
        viewModel.hotelStarRatingVisibilityObservable.subscribeVisibility(ratingBar)
        viewModel.starRatingColor.subscribeStarColor(ratingBar)
        viewModel.hotelStarRatingObservable.subscribeStarRating(ratingBar)
        viewModel.hotelAmenityOrDistanceVisibilityObservable.subscribeVisibility(amenityOrDistanceFromLocationTextView)
        viewModel.distanceFromCurrentLocation.subscribeText(amenityOrDistanceFromLocationTextView)
    }
}