package com.expedia.bookings.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.utils.bindView

interface OnHotelGuestRatingFilterChangedListener {
    fun onHotelGuestRatingFilterChanged(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean)
}

enum class GuestRatingValue(val trackingString: String) {
    Three("3"),
    Four("4"),
    Five("5")
}

class HotelGuestRatingFilterView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    @VisibleForTesting
    val filterThree: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_three)
    @VisibleForTesting
    val filterFour: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_four)
    @VisibleForTesting
    val filterFive: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_five)

    @VisibleForTesting
    var guestRatings = UserFilterChoices.GuestRatings()

    private var listener: OnHotelGuestRatingFilterChangedListener? = null

    init {
        View.inflate(context, R.layout.hotel_guest_rating_filter_view, this)
        setUpText()

        filterThree.clickedSubject.subscribe { toggleGuestRatingThree(true) }
        filterFour.clickedSubject.subscribe { toggleGuestRatingFour(true) }
        filterFive.clickedSubject.subscribe { toggleGuestRatingFive(true) }
    }

    fun setOnHotelGuestRatingFilterChangedListener(listener: OnHotelGuestRatingFilterChangedListener?) {
        this.listener = listener
    }

    fun reset() {
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
        guestRatings = UserFilterChoices.GuestRatings()
    }

    fun update(hotelGuestRating: UserFilterChoices.GuestRatings) {
        if (hotelGuestRating.three != filterThree.guestRatingSelected) toggleGuestRatingThree(false)
        if (hotelGuestRating.four != filterFour.guestRatingSelected) toggleGuestRatingFour(false)
        if (hotelGuestRating.five != filterFive.guestRatingSelected) toggleGuestRatingFive(false)
    }

    private fun toggleGuestRatingThree(doTracking: Boolean) {
        if (filterFour.guestRatingSelected) {
            toggleGuestRatingFour(doTracking)
        }
        if (filterFive.guestRatingSelected) {
            toggleGuestRatingFive(doTracking)
        }
        filterThree.toggle()
        guestRatings.three = filterThree.guestRatingSelected
        listener?.onHotelGuestRatingFilterChanged(GuestRatingValue.Three, filterThree.guestRatingSelected, doTracking)
    }

    private fun toggleGuestRatingFour(doTracking: Boolean) {
        if (filterThree.guestRatingSelected) {
            toggleGuestRatingThree(doTracking)
        }
        if (filterFive.guestRatingSelected) {
            toggleGuestRatingFive(doTracking)
        }
        filterFour.toggle()
        guestRatings.four = filterFour.guestRatingSelected
        listener?.onHotelGuestRatingFilterChanged(GuestRatingValue.Four, filterFour.guestRatingSelected, doTracking)
    }

    private fun toggleGuestRatingFive(doTracking: Boolean) {
        if (filterThree.guestRatingSelected) {
            toggleGuestRatingThree(doTracking)
        }
        if (filterFour.guestRatingSelected) {
            toggleGuestRatingFour(doTracking)
        }
        filterFive.toggle()
        guestRatings.five = filterFive.guestRatingSelected
        listener?.onHotelGuestRatingFilterChanged(GuestRatingValue.Five, filterFive.guestRatingSelected, doTracking)
    }

    private fun setUpText() {
        filterThree.filterGuestRating.text = context.resources.getString(R.string.guest_rating_three_and_above)
        filterFour.filterGuestRating.text = context.resources.getString(R.string.guest_rating_four_and_above)
        filterFive.filterGuestRating.text = context.resources.getString(R.string.guest_rating_five)
    }
}
