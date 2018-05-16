package com.expedia.bookings.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.utils.bindView

interface OnHotelStarRatingFilterChangedListener {
    fun onHotelStarRatingFilterChanged(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean)
}

enum class StarRatingValue(val trackingString: String) {
    One("1"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5")
}

class HotelStarRatingFilterView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    @VisibleForTesting
    val filterOne: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_one)
    @VisibleForTesting
    val filterTwo: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_two)
    @VisibleForTesting
    val filterThree: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_three)
    @VisibleForTesting
    val filterFour: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_four)
    @VisibleForTesting
    val filterFive: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_five)

    @VisibleForTesting
    var starRatings = UserFilterChoices.StarRatings()

    private var listener: OnHotelStarRatingFilterChangedListener? = null

    init {
        View.inflate(context, R.layout.hotel_star_rating_filter_view, this)

        filterOne.clickedSubject.subscribe { toggleStarOne(true) }
        filterTwo.clickedSubject.subscribe { toggleStarTwo(true) }
        filterThree.clickedSubject.subscribe { toggleStarThree(true) }
        filterFour.clickedSubject.subscribe { toggleStarFour(true) }
        filterFive.clickedSubject.subscribe { toggleStarFive(true) }
    }

    fun setOnHotelStarRatingFilterChangedListener(listener: OnHotelStarRatingFilterChangedListener?) {
        this.listener = listener
    }

    fun reset() {
        filterOne.deselect()
        filterTwo.deselect()
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
        starRatings = UserFilterChoices.StarRatings()
    }

    fun update(hotelStarRating: UserFilterChoices.StarRatings) {
        if (hotelStarRating.one != starRatings.one) {
            toggleStarOne(false)
        }
        if (hotelStarRating.two != starRatings.two) {
            toggleStarTwo(false)
        }
        if (hotelStarRating.three != starRatings.three) {
            toggleStarThree(false)
        }
        if (hotelStarRating.four != starRatings.four) {
            toggleStarFour(false)
        }
        if (hotelStarRating.five != starRatings.five) {
            toggleStarFive(false)
        }
    }

    private fun toggleStarOne(doTracking: Boolean) {
        filterOne.toggle()
        starRatings.one = filterOne.starSelected
        listener?.onHotelStarRatingFilterChanged(StarRatingValue.One, filterOne.starSelected, doTracking)
    }

    private fun toggleStarTwo(doTracking: Boolean) {
        filterTwo.toggle()
        starRatings.two = filterTwo.starSelected
        listener?.onHotelStarRatingFilterChanged(StarRatingValue.Two, filterTwo.starSelected, doTracking)
    }

    private fun toggleStarThree(doTracking: Boolean) {
        filterThree.toggle()
        starRatings.three = filterThree.starSelected
        listener?.onHotelStarRatingFilterChanged(StarRatingValue.Three, filterThree.starSelected, doTracking)
    }

    private fun toggleStarFour(doTracking: Boolean) {
        filterFour.toggle()
        starRatings.four = filterFour.starSelected
        listener?.onHotelStarRatingFilterChanged(StarRatingValue.Four, filterFour.starSelected, doTracking)
    }

    private fun toggleStarFive(doTracking: Boolean) {
        filterFive.toggle()
        starRatings.five = filterFive.starSelected
        listener?.onHotelStarRatingFilterChanged(StarRatingValue.Five, filterFive.starSelected, doTracking)
    }
}
